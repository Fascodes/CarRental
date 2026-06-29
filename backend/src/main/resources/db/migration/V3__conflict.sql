CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
ADD CONSTRAINT reservations_no_overlapping_confirmed
EXCLUDE USING gist (
    listing_id WITH =,
    tsrange(date_start, date_end, '[)') WITH &&
)
WHERE (status IN ('CONFIRMED', 'RENTER_CONFIRMED'));