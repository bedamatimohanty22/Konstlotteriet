-- Members table
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(50),
    membership_start_year INTEGER,
    last_win_year INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Artworks table
CREATE TABLE artworks (
    id BIGSERIAL PRIMARY KEY,
    year_id INTEGER NOT NULL,
    price_category VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    technique VARCHAR(255),
    size VARCHAR(100),
    purchase_year INTEGER NOT NULL,
    lottery_year INTEGER NOT NULL,
    is_won BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Lotteries table
CREATE TABLE lotteries (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL UNIQUE,
    lottery_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Lottery participants table
CREATE TABLE lottery_participants (
    id BIGSERIAL PRIMARY KEY,
    lottery_id BIGINT NOT NULL REFERENCES lotteries(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    number_of_tickets INTEGER NOT NULL CHECK (number_of_tickets >= 1 AND number_of_tickets <= 3),
    has_won BOOLEAN NOT NULL DEFAULT FALSE,
    won_artwork_id BIGINT REFERENCES artworks(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(lottery_id, member_id)
);

-- Ranking lists table
CREATE TABLE ranking_lists (
    id BIGSERIAL PRIMARY KEY,
    lottery_id BIGINT NOT NULL REFERENCES lotteries(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(lottery_id, member_id)
);

-- Ranking items table
CREATE TABLE ranking_items (
    id BIGSERIAL PRIMARY KEY,
    ranking_list_id BIGINT NOT NULL REFERENCES ranking_lists(id) ON DELETE CASCADE,
    artwork_id BIGINT NOT NULL REFERENCES artworks(id) ON DELETE CASCADE,
    rank INTEGER NOT NULL
);

-- Indexes for better performance
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_active ON members(active);
CREATE INDEX idx_artworks_lottery_year ON artworks(lottery_year);
CREATE INDEX idx_artworks_is_won ON artworks(is_won);
CREATE INDEX idx_lotteries_year ON lotteries(year);
CREATE INDEX idx_lotteries_status ON lotteries(status);
CREATE INDEX idx_lottery_participants_lottery_id ON lottery_participants(lottery_id);
CREATE INDEX idx_lottery_participants_member_id ON lottery_participants(member_id);
CREATE INDEX idx_ranking_lists_lottery_id ON ranking_lists(lottery_id);
CREATE INDEX idx_ranking_lists_member_id ON ranking_lists(member_id);
CREATE INDEX idx_ranking_items_ranking_list_id ON ranking_items(ranking_list_id);
