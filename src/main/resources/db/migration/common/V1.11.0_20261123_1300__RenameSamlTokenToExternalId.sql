-- The saml_token column stores the external identity identifier received from
-- the IdP (the OIDC subject claim). The SAML based integration was replaced
-- with OIDC, so the column and its unique index are renamed to match the
-- current meaning. Values are kept as they are: for accounts created through
-- bulk domain provisioning the column may still hold a temporary email
-- placeholder, replaced with the real subject on the first external login.

ALTER TABLE users RENAME COLUMN saml_token TO external_id;

ALTER TABLE users_AUD RENAME COLUMN saml_token TO external_id;

ALTER INDEX uq_users_saml_token RENAME TO uq_users_external_id;
