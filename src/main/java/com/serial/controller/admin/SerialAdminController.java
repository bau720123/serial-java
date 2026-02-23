package com.serial.controller.admin;

import com.serial.entity.SerialActivity;
import com.serial.entity.SerialDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 後台管理 Controller
 * 完全對應 Laravel 的 SerialAdminController
 */
@Controller
@RequestMapping("/admin/serials")
public class SerialAdminController {

    private final EntityManager entityManager;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SerialAdminController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * 後台列表頁面
     * 對應 Laravel: SerialAdminController@index
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer status,
            @RequestParam(name = "date_start", required = false) String dateStart,
            @RequestParam(name = "date_end", required = false) String dateEnd,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        // 分頁設定 (Laravel 預設 15 筆)
        Pageable pageable = PageRequest.of(page - 1, 15);

        // 查詢資料
        Page<SerialDetail> list = searchSerials(keyword, content, status, dateStart, dateEnd, pageable);

        // 不需要傳遞參數到 Model，Thymeleaf 可以直接使用 param
        model.addAttribute("list", list);

        return "admin/serials/index";
    }

    /**
     * CSV 匯出功能
     * 對應 Laravel: SerialAdminController@export
     * 使用分批查詢（chunk）避免記憶體溢位
     * 使用 AJAX 方式匯出，支援 Loading 效果
     */
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer status,
            @RequestParam(name = "date_start", required = false) String dateStart,
            @RequestParam(name = "date_end", required = false) String dateEnd,
            HttpServletResponse response) throws IOException {

        // 模擬耗時的匯出過程（測試 Loading 動畫）
        // try {
        //     TimeUnit.SECONDS.sleep(5);  // ← 延遲 5 秒
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        // }

        // 設定檔名
        String filename = "serial_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        // 設定 HTTP Header
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("X-Suggested-Filename", filename);  // 自訂 Header 給前端抓

        // 寫入 CSV（使用 UTF-8 BOM）
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
        writer.write('\ufeff'); // UTF-8 BOM

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader("活動名稱", "活動唯一ID", "序號", "狀態", "更新時間", "有效期限（起）", "有效期限（迄）", "備註說明", "新增時間")
                .setQuoteMode(QuoteMode.ALL)
                .build());

        // 使用分批查詢（chunk），每次 1000 筆，對應 Laravel 的 chunk(1000)
        int chunkSize = 1000;
        int pageNumber = 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, chunkSize);
            Page<SerialDetail> page = searchSerials(keyword, content, status, dateStart, dateEnd, pageable);

            // 寫入當前批次的資料
            for (SerialDetail detail : page.getContent()) {
                csvPrinter.printRecord(
                        detail.getSerialActivity().getActivityName(),
                        detail.getSerialActivity().getActivityUniqueId(),
                        detail.getContent(),
                        getStatusText(detail.getStatus()),
                        detail.getUpdatedAt() != null ? detail.getUpdatedAt().format(DTF) : "--",
                        detail.getStartDate().format(DTF),
                        detail.getEndDate().format(DTF),
                        detail.getNote() != null ? detail.getNote() : "-",
                        detail.getCreatedAt().format(DTF)
                );
            }

            // 沒有下一頁就結束
            if (!page.hasNext()) break;
            pageNumber++;
        }

        csvPrinter.flush();
        csvPrinter.close();
    }

    /**
     * 分頁查詢
     */
    private Page<SerialDetail> searchSerials(String keyword, String content, Integer status,
                                             String dateStart, String dateEnd, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SerialDetail> query = cb.createQuery(SerialDetail.class);
        Root<SerialDetail> root = query.from(SerialDetail.class);
        root.fetch("serialActivity", JoinType.INNER);

        List<Predicate> predicates = buildPredicates(cb, root, keyword, content, status, dateStart, dateEnd);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("id")));

        List<SerialDetail> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // 計算總筆數
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SerialDetail> countRoot = countQuery.from(SerialDetail.class);
        countRoot.join("serialActivity", JoinType.INNER);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, keyword, content, status, dateStart, dateEnd);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    /**
     * 建立查詢條件
     * 完全對應 Laravel 的查詢邏輯
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<SerialDetail> root,
                                            String keyword, String content, Integer status,
                                            String dateStart, String dateEnd) {
        List<Predicate> predicates = new ArrayList<>();
        Join<SerialDetail, SerialActivity> activityJoin = root.join("serialActivity", JoinType.INNER);

        // keyword：活動名稱 OR 活動唯一ID（模糊搜尋）
        if (keyword != null && !keyword.isBlank()) {
            Predicate namePredicate = cb.like(activityJoin.get("activityName"), "%" + keyword + "%");
            Predicate idPredicate = cb.like(activityJoin.get("activityUniqueId"), "%" + keyword + "%");
            predicates.add(cb.or(namePredicate, idPredicate));
        }

        // content：序號內容（精確搜尋，轉大寫）
        if (content != null && !content.isBlank()) {
            predicates.add(cb.equal(root.get("content"), content.trim().toUpperCase()));
        }

        // status：核銷狀況
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        // date_start / date_end：序號生效日/失效日範圍（對應 Laravel: where('start_date', '>=', ...) 和 where('end_date', '<=', ...)）
        if (dateStart != null && !dateStart.isBlank()) {
            LocalDateTime start = LocalDateTime.parse(dateStart + " 00:00:00", DTF);
            predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), start));
        }
        if (dateEnd != null && !dateEnd.isBlank()) {
            LocalDateTime end = LocalDateTime.parse(dateEnd + " 23:59:59", DTF);
            predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), end));
        }

        return predicates;
    }

    /**
     * 狀態文字轉換
     */
    private String getStatusText(int status) {
        return switch (status) {
            case 0 -> "未核銷";
            case 1 -> "已核銷";
            case 2 -> "已註銷";
            default -> "未設定";
        };
    }
}