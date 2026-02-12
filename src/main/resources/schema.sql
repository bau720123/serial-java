-- ============================================================
-- 序號管理系統 — SQL Server DDL
-- ============================================================

CREATE TABLE serial_activity (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    activity_name      NVARCHAR(255) NOT NULL,
    activity_unique_id NVARCHAR(100) NOT NULL,
    start_date         DATETIME NOT NULL,
    end_date           DATETIME NOT NULL,
    quota              INT NOT NULL,
    created_at         DATETIME DEFAULT GETDATE(),
    updated_at         DATETIME DEFAULT GETDATE(),

    CONSTRAINT UQ_ActivityUniqueID UNIQUE (activity_unique_id)
);

CREATE INDEX IX_serial_activity_unique_id ON serial_activity(activity_unique_id);
CREATE INDEX IX_serial_activity_dates ON serial_activity(start_date, end_date);

CREATE TABLE serial_detail (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    serial_activity_id INT NOT NULL,
    content            NVARCHAR(8) NOT NULL,
    status             INT NOT NULL DEFAULT 0,
    note               NVARCHAR(MAX) NULL,
    start_date         DATETIME NOT NULL,
    end_date           DATETIME NOT NULL,
    created_at         DATETIME DEFAULT GETDATE(),
    updated_at         DATETIME NULL,

    CONSTRAINT UQ_SerialContent UNIQUE (content),
    CONSTRAINT FK_serial_detail_activity
        FOREIGN KEY (serial_activity_id)
        REFERENCES serial_activity(id) ON DELETE CASCADE
);

CREATE INDEX IX_serial_detail_activity_id ON serial_detail(serial_activity_id);
CREATE INDEX IX_serial_detail_status ON serial_detail(status);
CREATE INDEX IX_serial_detail_dates ON serial_detail(start_date, end_date);
CREATE INDEX IX_serial_detail_content ON serial_detail(content);

CREATE TABLE serial_log (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    api_name    NVARCHAR(100) NOT NULL,
    host        NVARCHAR(50) NOT NULL,
    api         NVARCHAR(255) NOT NULL,
    request     NVARCHAR(MAX) NOT NULL,
    request_at  DATETIME NOT NULL,
    response    NVARCHAR(MAX) NULL,
    response_at DATETIME NULL,
    created_at  DATETIME DEFAULT GETDATE()
);

CREATE INDEX IX_serial_log_request_at ON serial_log(request_at);
CREATE INDEX IX_serial_log_api_name ON serial_log(api_name);
