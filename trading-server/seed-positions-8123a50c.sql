-- Adds sample positions to trading account:
-- 8123a50c-f8e4-4d39-ac3f-7f5931fa1ae2
--
-- FinTrade stores UUID entity identifiers as BINARY(16), so every textual
-- UUID must be converted with UUID_TO_BIN(...).

START TRANSACTION;

SET @trading_account_id = UUID_TO_BIN(
    '8123a50c-f8e4-4d39-ac3f-7f5931fa1ae2'
);

INSERT IGNORE INTO positions (
    id,
    created_at,
    updated_at,
    version,
    trading_account_id,
    stock_id,
    quantity,
    reserved_quantity,
    average_purchase_price
)
VALUES
    (
        UUID_TO_BIN(UUID()),
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6),
        0,
        @trading_account_id,
        UUID_TO_BIN('03d73f90-f26e-429e-97e2-ad3e7b9d9c5c'),
        20,
        0,
        260.0000
    ),
    (
        UUID_TO_BIN(UUID()),
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6),
        0,
        @trading_account_id,
        UUID_TO_BIN('18ddcd7e-d706-4665-b308-78b36f2f972a'),
        35,
        0,
        185.5000
    ),
    (
        UUID_TO_BIN(UUID()),
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6),
        0,
        @trading_account_id,
        UUID_TO_BIN('4874525d-7ee3-46dc-80e1-b51920bf6a3f'),
        12,
        0,
        230.2500
    ),
    (
        UUID_TO_BIN(UUID()),
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6),
        0,
        @trading_account_id,
        UUID_TO_BIN('99222c87-48ee-4b52-8b3f-d3ec9733c203'),
        15,
        0,
        470.0000
    ),
    (
        UUID_TO_BIN(UUID()),
        CURRENT_TIMESTAMP(6),
        CURRENT_TIMESTAMP(6),
        0,
        @trading_account_id,
        UUID_TO_BIN('a0db2435-0715-4242-8385-e0ce1437cbd6'),
        8,
        0,
        440.7500
    );

COMMIT;

-- Verify the positions that belong to the account.
SELECT
    BIN_TO_UUID(p.id) AS position_id,
    stock.symbol,
    p.quantity,
    p.reserved_quantity,
    p.average_purchase_price
FROM positions AS p
JOIN stocks AS stock
    ON stock.id = p.stock_id
WHERE p.trading_account_id = @trading_account_id
ORDER BY stock.symbol;
