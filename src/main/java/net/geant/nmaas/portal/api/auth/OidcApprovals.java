package net.geant.nmaas.portal.api.auth;

public record OidcApprovals(String oidcToken,
                            String email,
                            String password,
                            String uuid,
                            String firstName,
                            String lastName,
                            String username,
                            boolean isAupApprove,
                            boolean isPnApprove){}