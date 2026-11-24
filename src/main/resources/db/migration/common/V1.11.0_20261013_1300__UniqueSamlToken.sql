-- The saml_token column stores the external identity identifier (OIDC subject).
-- It must uniquely identify an account: a single external identity may only be
-- linked to one local account. Without this constraint duplicated identifiers
-- make the lookup by external identity ambiguous and break OIDC login.

-- normalize historical empty tokens
UPDATE users SET saml_token = NULL WHERE saml_token = '';

-- drop duplicated identifiers, keeping the link on the oldest account;
-- accounts that lose their identifier will go through the linking flow on the next SSO login
UPDATE users u SET saml_token = NULL
WHERE u.saml_token IS NOT NULL
  AND EXISTS (SELECT 1 FROM users d WHERE d.saml_token = u.saml_token AND d.id < u.id);

CREATE UNIQUE INDEX uq_users_saml_token ON users (saml_token) WHERE saml_token IS NOT NULL;
