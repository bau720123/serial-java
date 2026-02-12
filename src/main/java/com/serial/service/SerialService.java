package com.serial.service;

import com.serial.dto.request.*;
import com.serial.dto.response.*;
import com.serial.entity.SerialActivity;
import com.serial.entity.SerialDetail;
import com.serial.exception.BusinessException;
import com.serial.repository.SerialActivityRepository;
import com.serial.repository.SerialDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SerialService {

    private final SerialActivityRepository activityRepo;
    private final SerialDetailRepository detailRepo;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SerialService(SerialActivityRepository activityRepo, SerialDetailRepository detailRepo) {
        this.activityRepo = activityRepo;
        this.detailRepo = detailRepo;
    }

    @Transactional
    public SerialInsertResponseData insertSerials(SerialInsertRequest req) {
        validateInsert(req);

        SerialActivity activity = new SerialActivity();
        activity.setActivityName(req.getActivityName());
        activity.setActivityUniqueId(req.getActivityUniqueId());
        activity.setStartDate(req.getStartDate());
        activity.setEndDate(req.getEndDate());
        activity.setQuota(req.getQuota());
        activity = activityRepo.save(activity);

        int generated = generateAndSave(activity, req.getStartDate(), req.getEndDate(), req.getQuota(), null);
        return new SerialInsertResponseData(activity.getId(), generated);
    }

    @Transactional
    public SerialInsertResponseData additionalInsertSerials(SerialAdditionalInsertRequest req) {
        validateAdditionalInsert(req);

        SerialActivity activity = activityRepo.findByActivityUniqueId(req.getActivityUniqueId())
                .orElseThrow(() -> new BusinessException("所選擇的 活動唯一 ID 無效（該活動不存在）。"));

        activity.setStartDate(req.getStartDate());
        activity.setEndDate(req.getEndDate());
        activity.setQuota(activity.getQuota() + req.getQuota());
        activityRepo.save(activity);

        int generated = generateAndSave(activity, req.getStartDate(), req.getEndDate(), req.getQuota(), req.getNote());
        return new SerialInsertResponseData(activity.getId(), generated);
    }

    @Transactional
    public SerialRedeemResponseData redeemSerial(SerialRedeemRequest req) {
        String content = req.getContent().trim().toUpperCase();

        SerialDetail serial = detailRepo.findByContentWithLock(content)
                .orElseThrow(() -> new BusinessException("此序號不存在"));

        if (serial.getStatus() == SerialDetail.STATUS_USED) {
            throw new BusinessException("此序號已經被核銷使用");
        }
        if (serial.getStatus() == SerialDetail.STATUS_CANCELLED) {
            throw new BusinessException("此序號已被註銷，無法核銷");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(serial.getStartDate())) {
            throw new BusinessException("此序號尚未生效");
        }
        if (now.isAfter(serial.getEndDate())) {
            throw new BusinessException("此序號已過期");
        }

        serial.setStatus(SerialDetail.STATUS_USED);
        serial.setUpdatedAt(now);
        detailRepo.save(serial);

        return new SerialRedeemResponseData(serial.getContent(), now.format(FMT));
    }

    @Transactional
    public SerialCancelResponse cancelSerials(SerialCancelRequest req) {
        validateCancelContents(req.getContent());

        Set<String> contentSet = req.getContent().stream()
                .map(c -> c.trim().toUpperCase())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<SerialDetail> serials = detailRepo.findByContentInWithLock(contentSet);
        Map<String, SerialDetail> serialMap = serials.stream()
                .collect(Collectors.toMap(SerialDetail::getContent, s -> s));

        LocalDateTime now = LocalDateTime.now();
        List<String> successList = new ArrayList<>();
        List<String> failList = new ArrayList<>();

        for (String content : contentSet) {
            SerialDetail serial = serialMap.get(content);
            if (serial == null) {
                failList.add(content + " (此序號不存在)");
                continue;
            }
            if (serial.getStatus() == SerialDetail.STATUS_CANCELLED) {
                failList.add(content + " (此序號已被註銷，請勿重複註銷)");
                continue;
            }
            if (serial.getStatus() == SerialDetail.STATUS_USED) {
                failList.add(content + " (此序號已被核銷，無法再註銷)");
                continue;
            }
            serial.setStatus(SerialDetail.STATUS_CANCELLED);
            serial.setNote(req.getNote());
            serial.setUpdatedAt(now);
            successList.add(content);
        }

        if (!successList.isEmpty()) {
            List<SerialDetail> toUpdate = serials.stream()
                    .filter(s -> successList.contains(s.getContent()))
                    .collect(Collectors.toList());
            detailRepo.saveAll(toUpdate);
        }

        String message = failList.isEmpty() ? "全部註銷成功"
                : successList.isEmpty() ? "全部註銷失敗"
                : "部分註銷成功";

        SerialCancelResponse.CancelData successData = new SerialCancelResponse.CancelData(String.join(",", successList));
        SerialCancelResponse.CancelData failData = new SerialCancelResponse.CancelData(String.join(",", failList));

        return new SerialCancelResponse("success", message, now.format(FMT), successData, failData);
    }

    private void validateInsert(SerialInsertRequest req) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        if (activityRepo.existsByActivityUniqueId(req.getActivityUniqueId())) {
            errors.computeIfAbsent("activity_unique_id", k -> new ArrayList<>())
                    .add("活動唯一 ID 已存在，請勿重複新增。");
        }
        validateDates(req.getStartDate(), req.getEndDate(), errors);
        throwIfErrors(errors);
    }

    private void validateAdditionalInsert(SerialAdditionalInsertRequest req) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        if (!activityRepo.existsByActivityUniqueId(req.getActivityUniqueId())) {
            errors.computeIfAbsent("activity_unique_id", k -> new ArrayList<>())
                    .add("所選擇的 活動唯一 ID 無效（該活動不存在）。");
        }
        validateDates(req.getStartDate(), req.getEndDate(), errors);
        throwIfErrors(errors);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end, Map<String, List<String>> errors) {
        List<String> dateErrors = new ArrayList<>();
        if (start != null && end != null && end.isBefore(start)) {
            dateErrors.add("結束日期 必須晚於或等於 開始日期。");
        }
        if (end != null && end.isBefore(LocalDateTime.now())) {
            dateErrors.add("結束日期 不能早於當前時間，否則序號將立即過期。");
        }
        if (!dateErrors.isEmpty()) errors.put("end_date", dateErrors);
    }

    private void validateCancelContents(List<String> contents) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (int i = 0; i < contents.size(); i++) {
            String c = contents.get(i);
            if (c == null || c.trim().length() != 8) {
                errors.computeIfAbsent("content." + i, k -> new ArrayList<>())
                        .add("序號項目 [" + c + "] 必須是 8 個字元。");
            }
        }
        throwIfErrors(errors);
    }

    private int generateAndSave(SerialActivity activity, LocalDateTime startDate, LocalDateTime endDate, int quota, String note) {
        final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        Set<String> candidates = new LinkedHashSet<>();

        while (candidates.size() < quota) {
            char letter = LETTERS.charAt(random.nextInt(26));
            String digits = String.format("%07d", random.nextInt(10_000_000));
            candidates.add(letter + digits);
        }

        Set<String> existing = detailRepo.findExistingContents(candidates);
        candidates.removeAll(existing);

        while (candidates.size() < quota) {
            char letter = LETTERS.charAt(random.nextInt(26));
            String digits = String.format("%07d", random.nextInt(10_000_000));
            String c = letter + digits;
            if (!existing.contains(c)) candidates.add(c);
        }

        List<SerialDetail> details = new ArrayList<>();
        for (String content : candidates) {
            if (details.size() >= quota) break;
            SerialDetail detail = new SerialDetail();
            detail.setSerialActivity(activity);
            detail.setContent(content);
            detail.setStatus(SerialDetail.STATUS_UNUSED);
            detail.setNote(note);
            detail.setStartDate(startDate);
            detail.setEndDate(endDate);
            details.add(detail);
        }

        detailRepo.saveAll(details);
        return details.size();
    }

    private void throwIfErrors(Map<String, List<String>> errors) {
        if (!errors.isEmpty()) {
            Map<String, Object> apiErrors = new LinkedHashMap<>(errors);
            throw new ValidationException(apiErrors);
        }
    }

    public static class ValidationException extends RuntimeException {
        private final Map<String, Object> errors;

        public ValidationException(Map<String, Object> errors) {
            super("驗證失敗");
            this.errors = errors;
        }

        public Map<String, Object> getErrors() {
            return errors;
        }
    }
}
