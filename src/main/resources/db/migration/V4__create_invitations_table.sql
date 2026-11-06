CREATE TABLE invitations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token        UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    email        VARCHAR(255) NOT NULL,
    role         VARCHAR(20) NOT NULL,
    invited_by   UUID NOT NULL REFERENCES users(id),
    expires_at   TIMESTAMP NOT NULL,
    accepted_at  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invitations_token ON invitations(token);
CREATE INDEX idx_invitations_email ON invitations(email);
