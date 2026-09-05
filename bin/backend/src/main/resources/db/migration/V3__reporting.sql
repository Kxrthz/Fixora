CREATE VIEW provider_earnings AS
SELECT b.provider_id, COUNT(p.id) AS paid_jobs, COALESCE(SUM(p.amount),0) AS total_earnings
FROM bookings b LEFT JOIN payments p ON p.booking_id=b.id AND p.status='PAID'
GROUP BY b.provider_id;

CREATE PROCEDURE mark_notification_read(IN notification_id BIGINT, IN owner_id BIGINT)
UPDATE notifications SET `read`=TRUE WHERE id=notification_id AND user_id=owner_id;

CREATE TRIGGER booking_status_audit BEFORE UPDATE ON bookings
FOR EACH ROW SET NEW.status = COALESCE(NEW.status, OLD.status);

