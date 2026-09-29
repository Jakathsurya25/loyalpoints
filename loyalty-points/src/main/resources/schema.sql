-- Requires MySQL 8.0.16+ (CHECK constraints are enforced from that version)

CREATE TABLE IF NOT EXISTS tier (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  name             VARCHAR(30)  NOT NULL,
  min_points       BIGINT       NOT NULL,
  discount_percent DECIMAL(5,2) NOT NULL,
  CONSTRAINT uq_tier_name   UNIQUE (name),
  CONSTRAINT uq_tier_min    UNIQUE (min_points),
  CONSTRAINT chk_tier_min   CHECK (min_points >= 0),
  CONSTRAINT chk_tier_disc  CHECK (discount_percent BETWEEN 0 AND 100)
);

CREATE TABLE IF NOT EXISTS customer (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  name             VARCHAR(100) NOT NULL,
  email            VARCHAR(120) NOT NULL,
  phone            VARCHAR(20),
  lifetime_points  BIGINT       NOT NULL DEFAULT 0,
  available_points BIGINT       NOT NULL DEFAULT 0,
  tier_id          BIGINT       NOT NULL,
  created_at       DATETIME(6)  NOT NULL,
  CONSTRAINT uq_customer_email  UNIQUE (email),
  CONSTRAINT fk_customer_tier   FOREIGN KEY (tier_id) REFERENCES tier(id),
  CONSTRAINT chk_cust_lifetime  CHECK (lifetime_points >= 0),
  CONSTRAINT chk_cust_available CHECK (available_points >= 0 AND available_points <= lifetime_points)
);

CREATE TABLE IF NOT EXISTS purchase (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id          BIGINT        NOT NULL,
  amount               DECIMAL(12,2) NOT NULL,
  tier_discount_percent DECIMAL(5,2) NOT NULL,
  tier_discount_amount DECIMAL(12,2) NOT NULL,
  points_redeemed      BIGINT        NOT NULL DEFAULT 0,
  redemption_discount  DECIMAL(12,2) NOT NULL DEFAULT 0,
  final_amount         DECIMAL(12,2) NOT NULL,
  points_earned        BIGINT        NOT NULL DEFAULT 0,
  purchase_date        DATETIME(6)   NOT NULL,
  CONSTRAINT fk_purchase_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT chk_purchase_amount  CHECK (amount > 0),
  CONSTRAINT chk_purchase_final   CHECK (final_amount >= 0),
  CONSTRAINT chk_purchase_points  CHECK (points_redeemed >= 0 AND points_earned >= 0),
  INDEX idx_purchase_customer (customer_id, purchase_date)
);

CREATE TABLE IF NOT EXISTS points_ledger (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id   BIGINT       NOT NULL,
  purchase_id   BIGINT       NULL,
  entry_type    VARCHAR(10)  NOT NULL,
  points        BIGINT       NOT NULL,          -- +ve for EARN, -ve for REDEEM
  balance_after BIGINT       NOT NULL,
  description   VARCHAR(255),
  created_at    DATETIME(6)  NOT NULL,
  CONSTRAINT fk_ledger_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_ledger_purchase FOREIGN KEY (purchase_id) REFERENCES purchase(id),
  CONSTRAINT chk_ledger_type    CHECK ((entry_type = 'EARN' AND points > 0) OR (entry_type = 'REDEEM' AND points < 0)),
  CONSTRAINT chk_ledger_balance CHECK (balance_after >= 0),
  INDEX idx_ledger_customer (customer_id, created_at)
);
