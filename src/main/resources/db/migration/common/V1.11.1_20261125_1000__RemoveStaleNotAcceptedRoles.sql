-- ROLE_NOT_ACCEPTED is a transient marker: it is assigned to the user during
-- login when the AUP / Privacy Policy have not been accepted yet, and it is
-- removed from the database once the acceptance is recorded through
-- POST /users/terms/{username}. Because of a cascade from the login history
-- entity the marker used to be persisted together with the user, and it stayed
-- in the database after the acceptance. This migration removes the stale
-- marker from users who have already accepted both documents; the marker of
-- the remaining users is removed the next time they accept.

DELETE FROM user_role
WHERE role = 'ROLE_NOT_ACCEPTED'
  AND user_id IN (SELECT id
                  FROM users
                  WHERE terms_of_use_accepted = TRUE
                    AND privacy_policy_accepted = TRUE);
