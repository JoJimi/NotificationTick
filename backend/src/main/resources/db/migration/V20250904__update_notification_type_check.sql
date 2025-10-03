ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_type_check;

-- 최신 enum 값으로 재생성
ALTER TABLE notification
    ADD CONSTRAINT notification_type_check
        CHECK (type IN ('INTEREST_ADDED','NEWS_EVENT','PRICE_SURGE','PRICE_DROP'));