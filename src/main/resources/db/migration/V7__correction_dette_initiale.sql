ALTER TABLE transaction_compte_courant
    ADD COLUMN transaction_origine_id BIGINT;

ALTER TABLE transaction_compte_courant
    ADD CONSTRAINT fk_transaction_cc_origine
        FOREIGN KEY (transaction_origine_id)
        REFERENCES transaction_compte_courant(id);

CREATE INDEX idx_transaction_cc_origine
    ON transaction_compte_courant(transaction_origine_id);

ALTER TABLE transaction_compte_courant
    DROP CONSTRAINT IF EXISTS transaction_compte_courant_type_check;

ALTER TABLE transaction_compte_courant
    ADD CONSTRAINT transaction_compte_courant_type_check
        CHECK (type IN (
            'EMPRUNT',
            'DETTE_INITIALE',
            'ANNULATION_DETTE_INITIALE',
            'REMBOURSEMENT'
        ));
