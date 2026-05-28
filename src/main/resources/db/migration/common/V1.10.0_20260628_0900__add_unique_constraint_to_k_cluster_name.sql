DO $$
DECLARE
duplicate_names text;
BEGIN
SELECT string_agg(name || ' (' || count || ')', ', ')
INTO duplicate_names
FROM (
         SELECT name, COUNT(*) AS count
         FROM k_cluster
         GROUP BY name
         HAVING COUNT(*) > 1
         ORDER BY name
     ) duplicated;

IF duplicate_names IS NOT NULL THEN
        RAISE EXCEPTION
            'Cannot add unique constraint uq_k_cluster_name on k_cluster.name. Duplicate names exist: %',
            duplicate_names;
END IF;
END $$;

ALTER TABLE k_cluster
    ADD CONSTRAINT uq_k_cluster_name UNIQUE (name);