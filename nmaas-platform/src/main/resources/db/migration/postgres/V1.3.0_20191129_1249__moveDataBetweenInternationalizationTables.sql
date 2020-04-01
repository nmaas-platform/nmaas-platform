insert into internationalization_simple(id, enabled, language)
select id, enabled, language from internationalization;

insert into internationalization_simple_language_nodes(internationalization_simple_id, node_key, content)
with recursive parse_internationalization AS (
    select i.id,
           j.key,
           j.value
    from internationalization i, json_each(i.content::json) j

    UNION ALL

    select p.id,
           concat(p.key, '.', j.key),
           j.value
    from parse_internationalization p, json_each(p.value) j where json_typeof(p.value) != 'string'
) SELECT id, key, TRIM(BOTH '\"' FROM value::text)
from parse_internationalization where json_typeof(value) = 'string';