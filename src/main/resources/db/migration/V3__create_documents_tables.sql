CREATE TABLE documents (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title        VARCHAR(500) NOT NULL DEFAULT 'Untitled',
    content      TEXT,
    created_by   UUID NOT NULL REFERENCES users(id),
    folder_id    UUID,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE document_versions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    content     TEXT NOT NULL,
    created_by  UUID NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_workspace_id ON documents(workspace_id);
CREATE INDEX idx_documents_title ON documents(workspace_id, title);
CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);
