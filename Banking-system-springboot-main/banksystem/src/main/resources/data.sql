-- =======================================================
-- data.sql with YOUR WORKING BCrypt hash
-- Password: password123
-- Hash: $2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2
-- =======================================================

-- =======================================================
-- 1. CENTRAL BANK & ADMIN
-- =======================================================

INSERT INTO central_bank (id, name, code, address, contact_email, contact_phone, total_earning, created_at, updated_at)
VALUES (1, 'Reserve Bank of India', 'RBI001', 'Mumbai, India', 'admin@rbi.org.in', '022-12345678', 0.00, NOW(), NOW());

-- Login: superadmin / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (1, 'superadmin', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'CENTRALADMIN');

INSERT INTO central_bank_admin (id, full_name, username, email, password_hash, role, is_active, central_bank_id, user_id, created_at)
VALUES (1, 'Super Admin', 'superadmin', 'admin@rbi.org.in', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'central_admin', true, 1, 1, NOW());


-- =======================================================
-- 2. HEAD BANK & ADMIN
-- =======================================================

INSERT INTO head_bank (id, name, code, routing_number, address, contact_email, contact_phone, total_earning, is_active, central_bank_id, created_at, updated_at)
VALUES (1, 'State Bank of India', 'SBI001', 'RT-SBI-001', 'Nariman Point, Mumbai', 'contact@sbi.co.in', '1800-11-2211', 0.00, true, 1, NOW(), NOW());

-- Login: headadmin_sbi / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (2, 'headadmin_sbi', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'HEADMANAGER');

INSERT INTO head_bank_admin (id, full_name, username, email, password_hash, role, is_active, head_bank_id, user_id, created_at)
VALUES (1, 'Rajesh Kumar', 'headadmin_sbi', 'rajesh@sbi.co.in', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'head_bank_admin', true, 1, 2, NOW());


-- =======================================================
-- 3. BRANCH & MANAGER
-- =======================================================

INSERT INTO branch (id, name, branch_code, ifsc_code, address, contact_email, contact_phone, total_earning, is_active, head_bank_id, created_at, updated_at)
VALUES (1, 'Connaught Place Branch', 'BR001', 'SBIN0001234', 'Connaught Place, New Delhi', 'cp.branch@sbi.co.in', '011-23456789', 0.00, true, 1, NOW(), NOW());

-- Login: manager_cp / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (3, 'manager_cp', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'BRANCHMANAGER');

-- Note: Branch Manager Entity table insert was commented out in your original code, kept as is.
-- INSERT INTO branch_manager (id, full_name, username, email, password_hash, role, is_active, branch_id, user_id, created_at)
-- VALUES (1, 'Amit Sharma', 'manager_cp', 'amit.sharma@sbi.co.in', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'branch_manager', true, 1, 3, NOW());


-- =======================================================
-- 4. TELLERS
-- =======================================================

-- Login: teller_john / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (4, 'teller_john', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'TELLER');

INSERT INTO teller (id, full_name, username, email, password_hash, role, is_active, branch_id, teller_account_id, teller_account_number, user_id, created_at)
VALUES (1, 'John Doe', 'teller_john', 'john.doe@sbi.co.in', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'teller', true, 1, 999, 'TELLER-ACC-001', 4, NOW());


-- =======================================================
-- 5. CUSTOMERS & ACCOUNTS
-- =======================================================

-- Login: alice_wonder / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (5, 'alice_wonder', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'CUSTOMER');

INSERT INTO customer (id, customer_id, first_name, last_name, email, phone, address, aadhar_number, pan_number, is_active, transaction_pin_hash, branch_id, user_id, created_at, updated_at)
VALUES (1, 'CUST001', 'Alice', 'Wonderland', 'alice@gmail.com', '9876543210', '123 Baker Street', '1234-5678-9012', 'ABCDE1234F', true, '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 1, 5, NOW(), NOW());

INSERT INTO account (id, account_number, account_type, current_balance, available_balance, currency, status, customer_id, branch_id, account_holder_type, opened_date, created_at, updated_at)
VALUES (1, 'ACC1000001', 'SAVINGS', 50000.00, 50000.00, 'INR', 'ACTIVE', 1, 1, 'CUSTOMER', NOW(), NOW(), NOW());


-- Login: bob_builder / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (6, 'bob_builder', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'CUSTOMER');

INSERT INTO customer (id, customer_id, first_name, last_name, email, phone, address, aadhar_number, pan_number, is_active, transaction_pin_hash, branch_id, user_id, created_at, updated_at)
VALUES (2, 'CUST002', 'Bob', 'Builder', 'bob@construction.com', '9123456789', '456 Brick Lane', '9876-5432-1098', 'FGHIJ5678K', true, '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 1, 6, NOW(), NOW());

INSERT INTO account (id, account_number, account_type, current_balance, available_balance, currency, status, customer_id, branch_id, account_holder_type, opened_date, created_at, updated_at)
VALUES (2, 'ACC2000002', 'CURRENT', 100000.00, 100000.00, 'INR', 'ACTIVE', 2, 1, 'CUSTOMER', NOW(), NOW(), NOW());


-- =======================================================
-- 6. LOAN OFFERS & DEBIT CARD RULES
-- =======================================================

INSERT INTO loan_offers (id, offer_name, loan_type, interest_rate, min_amount, max_amount, min_tenure_months, max_tenure_months, eligibility_criteria, is_active, head_bank_id, created_at, updated_at)
VALUES (1, 'Super Saver Personal Loan', 'PERSONAL', 10.5, 50000, 1000000, 12, 60, 'Salary > 25000', true, 1, NOW(), NOW());

INSERT INTO debit_card_rules (id, card_type, daily_withdrawal_limit, daily_transaction_limit, annual_fee, international_usage, is_active, head_bank_id, created_at)
VALUES (1, 'VISA_GOLD', 50000.00, 100000.00, 500.00, true, true, 1, NOW());


-- =======================================================
-- 7. TRANSACTIONS
-- =======================================================

INSERT INTO transactions (id, transaction_reference, from_account_id, to_account_id, amount, transaction_type, status, description, fee_amount, net_amount, total_charges, transaction_date, created_at)
VALUES (1, 'TXN-REF-001', 1, 2, 5000.00, 'TRANSFER', 'COMPLETED', 'Rent Payment', 0.00, 5000.00, 0.00, NOW(), NOW());

UPDATE account SET current_balance = 45000.00, available_balance = 45000.00 WHERE id = 1;
UPDATE account SET current_balance = 105000.00, available_balance = 105000.00 WHERE id = 2;


-- =======================================================
-- 8. CHARGES BOOK (Fee Configuration)
-- =======================================================

INSERT INTO charges_book (id, fee_name, fee_amount, bank_id, bank_type, transaction_type, min_value, max_value, is_active, created_at, updated_at)
VALUES (1, 'IMPS Fee', 5.00, 1, 'HEAD_BANK', 'TRANSFER', 0, 100000, true, NOW(), NOW());


-- =======================================================
-- ADDING NEW HEAD BANK (HDFC), BRANCH, STAFF AND CUSTOMER
-- =======================================================

-- 1. ADD NEW HEAD BANK (ID: 2)
-- =======================================================
INSERT INTO head_bank (id, name, code, routing_number, address, contact_email, contact_phone, total_earning, is_active, central_bank_id, created_at, updated_at)
VALUES (2, 'HDFC Bank', 'HDFC001', 'RT-HDFC-001', 'Senapati Bapat Marg, Mumbai', 'contact@hdfc.com', '1800-22-3344', 0.00, true, 1, NOW(), NOW());

-- Login: headadmin_hdfc / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (7, 'headadmin_hdfc', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'HEADMANAGER');

-- Head Bank Admin Entity
INSERT INTO head_bank_admin (id, full_name, username, email, password_hash, role, is_active, head_bank_id, user_id, created_at)
VALUES (2, 'Suresh Menon', 'headadmin_hdfc', 'suresh@hdfc.com', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'head_bank_admin', true, 2, 7, NOW());


-- 2. ADD NEW BRANCH FOR HDFC (ID: 2)
-- =======================================================
INSERT INTO branch (id, name, branch_code, ifsc_code, address, contact_email, contact_phone, total_earning, is_active, head_bank_id, created_at, updated_at)
VALUES (2, 'Indiranagar Branch', 'BR002', 'HDFC0005678', 'Indiranagar, Bangalore', 'indiranagar@hdfc.com', '080-12345678', 0.00, true, 2, NOW(), NOW());

-- Login: manager_indiranagar / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (8, 'manager_indiranagar', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'BRANCHMANAGER');

-- Branch Manager Entity
INSERT INTO branch_manager (id, full_name, username, email, password_hash, role, is_active, branch_id, user_id, created_at)
VALUES (2, 'Priya Reddy', 'manager_indiranagar', 'priya.reddy@hdfc.com', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'branch_manager', true, 2, 8, NOW());


-- 3. ADD NEW TELLER FOR HDFC BRANCH (ID: 2)
-- =======================================================
-- Login: teller_vikram / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (9, 'teller_vikram', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'TELLER');

-- Teller Entity
INSERT INTO teller (id, full_name, username, email, password_hash, role, is_active, branch_id, teller_account_id, teller_account_number, user_id, created_at)
VALUES (2, 'Vikram Singh', 'teller_vikram', 'vikram.singh@hdfc.com', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'teller', true, 2, 888, 'TELLER-ACC-002', 9, NOW());


-- 4. ADD NEW CUSTOMER FOR HDFC BRANCH
-- =======================================================
-- Login: charlie_chaplin / Password: password123
INSERT INTO users (id, username, password, role)
VALUES (10, 'charlie_chaplin', '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 'CUSTOMER');

-- Customer Entity
INSERT INTO customer (id, customer_id, first_name, last_name, email, phone, address, aadhar_number, pan_number, is_active, transaction_pin_hash, branch_id, user_id, created_at, updated_at)
VALUES (3, 'CUST003', 'Charlie', 'Chaplin', 'charlie@movies.com', '7778889990', 'Hollywood Blvd, Mumbai', '1122-3344-5566', 'ZZYYX9876L', true, '$2a$10$GlkVuX7F0T2yZQfD/K9H/uNBX7key//aczyVOiv4Zzo0HbkL.ztO2', 2, 10, NOW(), NOW());

-- Customer Account (HDFC Savings)
INSERT INTO account (id, account_number, account_type, current_balance, available_balance, currency, status, customer_id, branch_id, account_holder_type, opened_date, created_at, updated_at)
VALUES (3, 'ACC3000003', 'SAVINGS', 75000.00, 75000.00, 'INR', 'ACTIVE', 3, 2, 'CUSTOMER', NOW(), NOW(), NOW());


-- 5. ADD LOAN OFFERS & RULES FOR HDFC
-- =======================================================
INSERT INTO loan_offers (id, offer_name, loan_type, interest_rate, min_amount, max_amount, min_tenure_months, max_tenure_months, eligibility_criteria, is_active, head_bank_id, created_at, updated_at)
VALUES (2, 'HDFC Dream Home Loan', 'HOME', 8.5, 1000000, 5000000, 60, 240, 'Salary > 50000', true, 2, NOW(), NOW());

INSERT INTO debit_card_rules (id, card_type, daily_withdrawal_limit, daily_transaction_limit, annual_fee, international_usage, is_active, head_bank_id, created_at)
VALUES (2, 'MASTERCARD_PLATINUM', 100000.00, 200000.00, 1000.00, true, true, 2, NOW());

-- 6. ADD CHARGES FOR HDFC
-- =======================================================
INSERT INTO charges_book (id, fee_name, fee_amount, bank_id, bank_type, transaction_type, min_value, max_value, is_active, created_at, updated_at)
VALUES (2, 'NEFT Fee', 2.50, 2, 'HEAD_BANK', 'TRANSFER', 0, 500000, true, NOW(), NOW());