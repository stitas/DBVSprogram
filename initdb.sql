-- === Lentos ===

CREATE TABLE klientas (
    id           SERIAL       PRIMARY KEY,
    vardas       VARCHAR(50)  NOT NULL,
    pavarde      VARCHAR(50)  NOT NULL,
    el_pastas    VARCHAR(255) NOT NULL UNIQUE,
    gimimo_data  DATE         NOT NULL CHECK (age(gimimo_data) >= INTERVAL '18 years')
);

CREATE TABLE automobilis (
    id               SERIAL       PRIMARY KEY,
    spalva           VARCHAR(30)  NOT NULL,
    registracijos_nr VARCHAR(6)   NOT NULL UNIQUE,
    marke            VARCHAR(50)  NOT NULL,
    kaina_parai      NUMERIC(5,2) NOT NULL CHECK (kaina_parai > 0)
);

CREATE TABLE nuoma (
    id          SERIAL        PRIMARY KEY,
    klientas_id INTEGER       NOT NULL,
    kaina       NUMERIC(7,2)  NOT NULL CHECK (kaina > 0),
    busena      VARCHAR(30)   NOT NULL CHECK (busena IN ('AKTYVI', 'LAUKIA_APMOKEJIMO', 'APMOKETA')) DEFAULT 'AKTYVI'
);

CREATE TABLE nuoma_automobilis (
    id             SERIAL  PRIMARY KEY,
    nuoma_id       INTEGER NOT NULL,
    pradzios_data  DATE    NOT NULL,
    pabaigos_data  DATE	   NOT NULL,
    atsiliepimas   TEXT,
    automobilis_id INTEGER NOT NULL
);

CREATE TABLE mokejimas (
    id             SERIAL        PRIMARY KEY,
    nuoma_id       INTEGER       NOT NULL,
    data           DATE          NOT NULL,
    suma           NUMERIC(7,2)  NOT NULL CHECK (suma > 0),
    mokejimo_budas VARCHAR(30)   NOT NULL CHECK (mokejimo_budas IN ('KORTELE', 'GRYNI', 'PAVEDIMAS')) DEFAULT 'KORTELE'
);

-- === Isoriniai raktai ===

ALTER TABLE nuoma
    ADD CONSTRAINT fk_nuoma_klientas
        FOREIGN KEY (klientas_id)
        REFERENCES klientas (id)
        ON DELETE CASCADE;

ALTER TABLE mokejimas
    ADD CONSTRAINT fk_mokejimas_nuoma
        FOREIGN KEY (nuoma_id)
        REFERENCES nuoma (id)
        ON DELETE CASCADE;

ALTER TABLE nuoma_automobilis
    ADD CONSTRAINT fk_nuoma_automobilis_automobilis
        FOREIGN KEY (automobilis_id)
        REFERENCES automobilis (id)
        ON DELETE CASCADE;

ALTER TABLE nuoma_automobilis
    ADD CONSTRAINT fk_nuoma_automobilis_nuoma
        FOREIGN KEY (nuoma_id)
        REFERENCES nuoma (id)
        ON DELETE CASCADE;

-- === Indeksai ===

CREATE INDEX idx_nuoma_busena
    ON nuoma (busena);

CREATE UNIQUE INDEX uq_klientas_vardas_pavarde_gimimo
    ON klientas (vardas, pavarde, gimimo_data);

-- === Dalykines taisykles ===
CREATE FUNCTION ar_galimas_atsiliepimas()
RETURNS trigger as $$
DECLARE
	nuomos_busena varchar;
BEGIN
	SELECT n.busena
	INTO nuomos_busena
	FROM auto_nuoma.nuoma n
	WHERE n.id = NEW.nuoma_id;

	IF NEW.atsiliepimas IS NOT NULL AND nuomos_busena <> 'APMOKETA' THEN
		RAISE EXCEPTION 'Busena turi buti APMOKETA, o yra %', nuomos_busena;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER nuoma_apmoketa_pries_atsiliepima
BEFORE INSERT OR UPDATE ON nuoma_automobilis
FOR EACH ROW
EXECUTE FUNCTION ar_galimas_atsiliepimas();

CREATE FUNCTION ar_galima_nauja_nuoma()
RETURNS trigger as $$
DECLARE
	nuomu_sk int;
BEGIN
	SELECT count(*)
	INTO nuomu_sk
	FROM auto_nuoma.nuoma n
	WHERE n.klientas_id = NEW.klientas_id
	AND n.busena <> 'APMOKETA';

	IF nuomu_sk >= 2 THEN
		RAISE EXCEPTION 'Klientas jau turi 2 ar daugiau neužbaigtų/neapmokėtų nuomų.';
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER galima_nauja_nuoma
BEFORE INSERT ON nuoma
FOR EACH ROW
EXECUTE FUNCTION ar_galima_nauja_nuoma();


CREATE OR REPLACE FUNCTION ar_galimas_auto_nuomai()
RETURNS trigger as $$
DECLARE
	egzistuoja boolean;
BEGIN
	SELECT EXISTS (
        SELECT 1
        FROM nuoma_automobilis na
        WHERE na.automobilis_id = NEW.automobilis_id
          AND (
			  (NEW.pradzios_data <= na.pradzios_data AND NEW.pabaigos_data >= na.pradzios_data)
			  OR (NEW.pradzios_data >= na.pradzios_data AND NEW.pabaigos_data <= na.pabaigos_data)
          	  OR (NEW.pradzios_data <= na.pabaigos_data AND NEW.pabaigos_data >= na.pabaigos_data)
			  OR (NEW.pradzios_data <= na.pradzios_data AND NEW.pabaigos_data >= na.pabaigos_data)
          )
    )
    INTO egzistuoja;

	IF egzistuoja THEN
		RAISE EXCEPTION 'Automobilis % jau išnuomotas tomis datomis (persidengia nuoma_automobilis įrašas)', NEW.automobilis_id;
	END IF;

	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER galimas_auto_nuomai
BEFORE INSERT ON nuoma_automobilis
FOR EACH ROW
EXECUTE FUNCTION ar_galimas_auto_nuomai();

-- === DUOMENYS ===
INSERT INTO klientas (id, vardas, pavarde, el_pastas, gimimo_data) VALUES
    (nextval('klientas_id_seq'), 'Jonas',  'Jonaitis',       'jonas@example.com',  '1990-05-10'),
    (nextval('klientas_id_seq'), 'Ieva',   'Petrauskaitė',   'ieva@example.com',   '1988-11-22'),
    (nextval('klientas_id_seq'), 'Mantas', 'Kazlauskas',     'mantas@example.com', '1995-02-03'),
    (nextval('klientas_id_seq'), 'Rasa',   'Vaitkienė',      'rasa@example.com',   '1982-07-19'),
    (nextval('klientas_id_seq'), 'Tomas',  'Jankus',         'tomas@example.com',  '1999-09-30'),
    (nextval('klientas_id_seq'), 'Greta',  'Balčiūnaitė',    'greta@example.com',  '1993-01-14'),
    (nextval('klientas_id_seq'), 'Darius', 'Žukauskas',      'darius@example.com', '1985-12-01'),
    (nextval('klientas_id_seq'), 'Aistė',  'Sabaliauskaitė', 'aiste@example.com',  '1997-04-08');

INSERT INTO automobilis (id, spalva, registracijos_nr, marke, kaina_parai) VALUES
    (nextval('automobilis_id_seq'), 'Juoda',      'ABC123', 'Audi A4',             45.00),
    (nextval('automobilis_id_seq'), 'Balta',      'DEF456', 'Volkswagen Golf',     35.00),
    (nextval('automobilis_id_seq'), 'Mėlyna',     'GHI789', 'BMW 320',             55.00),
    (nextval('automobilis_id_seq'), 'Sidabrinė',  'JKL321', 'Toyota Corolla',      30.00),
    (nextval('automobilis_id_seq'), 'Raudona',    'MNO654', 'Mazda 6',             40.00),
    (nextval('automobilis_id_seq'), 'Pilka',      'PQR987', 'Škoda Octavia',       32.00),
    (nextval('automobilis_id_seq'), 'Žalia',      'LOL206', 'Peugeot 206',         20.00);

INSERT INTO nuoma (id, klientas_id, kaina, busena) VALUES
    (nextval('nuoma_id_seq'), 1, 150.00, 'APMOKETA'),
    (nextval('nuoma_id_seq'), 2, 210.00, 'APMOKETA'),
    (nextval('nuoma_id_seq'), 3, 90.00, 'LAUKIA_APMOKEJIMO'),
    (nextval('nuoma_id_seq'), 4, 120.00, 'AKTYVI'),
    (nextval('nuoma_id_seq'), 5, 200.00, 'APMOKETA'),
    (nextval('nuoma_id_seq'), 6, 75.00, 'AKTYVI'),
    (nextval('nuoma_id_seq'), 7, 60.00, 'LAUKIA_APMOKEJIMO'),
    (nextval('nuoma_id_seq'), 7, 70.00, 'LAUKIA_APMOKEJIMO'),
    (nextval('nuoma_id_seq'), 8, 300.00, 'APMOKETA');

INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
    (nextval('nuoma_automobilis_id_seq'), 1, '2025-03-01', '2025-03-04', 'Labai tvarkingas automobilis, rekomenduoju.', 1),
    (nextval('nuoma_automobilis_id_seq'), 2, '2025-04-10', '2025-04-12', 'Geras kainos ir kokybės santykis.', 2),
    (nextval('nuoma_automobilis_id_seq'), 2, '2025-04-10', '2025-04-12', NULL, 3),
    (nextval('nuoma_automobilis_id_seq'), 3, '2025-12-01', '2025-12-03', NULL, 4),
    (nextval('nuoma_automobilis_id_seq'), 4, '2025-12-01', '2025-12-05', NULL, 5),
    (nextval('nuoma_automobilis_id_seq'), 5, '2025-02-15', '2025-02-18', 'Viskas sklandžiai, automobilis švarus.', 5),
    (nextval('nuoma_automobilis_id_seq'), 6, '2025-11-20', '2025-12-28', NULL, 2),
    (nextval('nuoma_automobilis_id_seq'), 7, '2025-12-01', '2025-12-02', NULL, 3),
    (nextval('nuoma_automobilis_id_seq'), 9, '2025-01-05', '2025-01-10', 'Viskas puikiai, aptarnavimas super.', 6),
    (nextval('nuoma_automobilis_id_seq'), 1, '2025-03-01', '2025-03-02', NULL, 4),
    (nextval('nuoma_automobilis_id_seq'), 8, '2025-11-02', '2025-11-28', NULL, 7);

INSERT INTO mokejimas (id, nuoma_id, data, suma, mokejimo_budas) VALUES
    (nextval('mokejimas_id_seq'), 1, '2025-02-28', 150.00, 'KORTELE'),
    (nextval('mokejimas_id_seq'), 2, '2025-04-09', 140.00, 'PAVEDIMAS'),
    (nextval('mokejimas_id_seq'), 5, '2025-02-14', 200.00, 'KORTELE'),
    (nextval('mokejimas_id_seq'), 8, '2025-01-03', 300.00, 'GRYNI'),
    (nextval('mokejimas_id_seq'), 2, '2025-04-15', 70.00, 'PAVEDIMAS');


-- === Virtualios lenteles ===
CREATE VIEW vw_neuzbaigtos_nuomos AS
SELECT
    n.id              AS nuoma_id,
    k.vardas,
    k.pavarde,
    a.marke,
    a.registracijos_nr,
    na.pradzios_data,
    na.pabaigos_data,
    n.busena
FROM nuoma n
JOIN klientas k
    ON k.id = n.klientas_id
JOIN nuoma_automobilis na
    ON na.nuoma_id = n.id
JOIN automobilis a
    ON a.id = na.automobilis_id
WHERE n.busena IN ('AKTYVI', 'LAUKIA_APMOKEJIMO');

CREATE MATERIALIZED VIEW mv_klientu_santrauka AS
WITH nuoma_s_mokejimais AS (
    -- Bendra suma už kiekvieną nuomą
    SELECT
        n.id AS nuoma_id,
        n.klientas_id,
        n.kaina AS nuomos_kaina,
        COALESCE(SUM(m.suma), 0) AS sumoketa_suma
    FROM nuoma n
    LEFT JOIN mokejimas m 
    ON m.nuoma_id = n.id
    GROUP BY n.id
),
nuomos_datos AS (
    -- Kiekvienos nuomos pradžios/pabaigos suvestinė
    SELECT
        n.id AS nuoma_id,
        MIN(na.pradzios_data) AS nuomos_pradzia,
        MAX(na.pabaigos_data) AS nuomos_pabaiga
    FROM nuoma n
    JOIN nuoma_automobilis na 
    ON na.nuoma_id = n.id
    GROUP BY n.id
)
SELECT
    k.id AS klientas_id,
    k.vardas,
    k.pavarde,
    k.el_pastas,

    -- kiek nuomų turi klientas
    COUNT(DISTINCT n.id) AS nuomu_skaicius,

    -- kiek skirtingų automobilių nuomavosi
    COUNT(DISTINCT na.automobilis_id) AS skirtingu_automobiliu,

    -- bendra visų nuomų kaina
    COALESCE(SUM(ns.nuomos_kaina), 0) AS bendra_nuomu_kaina,

    -- bendra sumokėta suma
    COALESCE(SUM(ns.sumoketa_suma), 0) AS bendra_sumoketa,

    -- bendras kliento „minusas“
    (COALESCE(SUM(ns.nuomos_kaina), 0) - COALESCE(SUM(ns.sumoketa_suma), 0))
        AS bendra_skolinga,

    -- kada buvo pirmoji nuoma
    MIN(nd.nuomos_pradzia) AS pirmos_nuomos_pradzia,

    -- kada buvo paskutinės nuomos pabaiga
    MAX(nd.nuomos_pabaiga) AS paskutines_nuomos_pabaiga

FROM klientas k
LEFT JOIN nuoma n
ON n.klientas_id = k.id
LEFT JOIN nuoma_automobilis na
ON na.nuoma_id = n.id
LEFT JOIN nuoma_s_mokejimais ns
ON ns.nuoma_id = n.id
LEFT JOIN nuomos_datos nd
ON nd.nuoma_id = n.id
GROUP BY k.id, k.vardas, k.pavarde, k.el_pastas;

--REFRESH MATERIALIZED VIEW mv_klientu_santrauka;


-- === Dalykines taisykles test ===
--UPDATE nuoma_automobilis
--SET atsiliepimas = 'ATSILIEPIMAS'
--WHERE id = 4;
--
--INSERT INTO nuoma (id, klientas_id, kaina, busena) VALUES
--    (nextval('nuoma_id_seq'), 7, 150.00, 'AKTYVI');

--INSERT INTO auto_nuoma.nuoma_automobilis
--(id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id)
--VALUES(nextval('nuoma_automobilis_id_seq'), 8, '2024-03-02', '2024-03-05', NULL, 1);


-- === VIEW TEST ===
-- Vieno automobilio visos nuomos (VIEW)
--SELECT *
--FROM vw_neuzbaigtos_nuomos
--ORDER BY pradzios_data;

-- Klientu santrauka (MATERIALIZED VIEW)
--SELECT *
--FROM mv_klientu_santrauka
--ORDER BY bendra_skolinga DESC;

-- TESTAI AUTO DATU PERSIDENGIMU TRIGGERIUI
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-03-01', '2025-03-04', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-02-20', '2025-03-04', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-03-01', '2025-03-08', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-03-02', '2025-03-03', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-02-20', '2025-03-20', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--INSERT INTO nuoma_automobilis (id, nuoma_id, pradzios_data, pabaigos_data, atsiliepimas, automobilis_id) VALUES
--    (nextval('nuoma_automobilis_id_seq'), 1, '2025-05-20', '2025-06-20', 'Labai tvarkingas automobilis, rekomenduoju.', 1);
--
--SELECT a.id AS automobilis_id
--FROM automobilis a
--WHERE NOT EXISTS (
--    SELECT 1
--    FROM nuoma_automobilis na
--    WHERE na.automobilis_id = a.id
--      AND na.pradzios_data <= DATE '2025-03-20'
--      AND na.pabaigos_data >= DATE '2025-02-20'
--);


