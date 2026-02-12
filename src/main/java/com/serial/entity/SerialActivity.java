package com.serial.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 活動主表 Entity
 * DDL: serial_activity
 */
@Entity
@Table(
    name = "serial_activity",
    uniqueConstraints = @UniqueConstraint(name = "UQ_ActivityUniqueID", columnNames = "activity_unique_id"),
    indexes = {
        @Index(name = "IX_serial_activity_unique_id", columnList = "activity_unique_id"),
        @Index(name = "IX_serial_activity_dates", columnList = "start_date, end_date")
    }
)
public class SerialActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "activity_name", nullable = false, length = 255)
    private String activityName;

    @Column(name = "activity_unique_id", nullable = false, length = 100, unique = true)
    private String activityUniqueId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "quota", nullable = false)
    private Integer quota;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "serialActivity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SerialDetail> serialDetails;

    // Constructors
    public SerialActivity() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getActivityUniqueId() { return activityUniqueId; }
    public void setActivityUniqueId(String activityUniqueId) { this.activityUniqueId = activityUniqueId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getQuota() { return quota; }
    public void setQuota(Integer quota) { this.quota = quota; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SerialDetail> getSerialDetails() { return serialDetails; }
    public void setSerialDetails(List<SerialDetail> serialDetails) { this.serialDetails = serialDetails; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerialActivity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
