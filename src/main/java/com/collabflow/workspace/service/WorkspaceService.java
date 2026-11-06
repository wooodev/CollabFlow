package com.collabflow.workspace.service;

import com.collabflow.auth.domain.User;
import com.collabflow.auth.repository.UserRepository;
import com.collabflow.common.exception.BusinessException;
import com.collabflow.workspace.domain.Workspace;
import com.collabflow.workspace.domain.WorkspaceMember;
import com.collabflow.workspace.domain.WorkspaceMember.MemberRole;
import com.collabflow.workspace.dto.InviteMemberRequest;
import com.collabflow.workspace.dto.WorkspaceCreateRequest;
import com.collabflow.workspace.dto.WorkspaceResponse;
import com.collabflow.workspace.repository.WorkspaceMemberRepository;
import com.collabflow.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse create(WorkspaceCreateRequest request, UUID userId) {
        Workspace workspace = Workspace.create(request.getName(), userId);
        workspaceRepository.save(workspace);

        WorkspaceMember ownerMember = WorkspaceMember.of(workspace.getId(), userId, MemberRole.OWNER);
        memberRepository.save(ownerMember);

        return WorkspaceResponse.of(workspace, MemberRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> findAll(UUID userId) {
        return workspaceRepository.findAllByMemberUserId(userId).stream()
                .map(ws -> {
                    MemberRole role = memberRepository
                            .findByIdWorkspaceIdAndIdUserId(ws.getId(), userId)
                            .map(WorkspaceMember::getRole)
                            .orElse(MemberRole.VIEWER);
                    return WorkspaceResponse.of(ws, role);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse findById(UUID workspaceId, UUID userId) {
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        WorkspaceMember member = getMemberOrThrow(workspaceId, userId);
        return WorkspaceResponse.of(workspace, member.getRole());
    }

    @Transactional
    public void delete(UUID workspaceId, UUID userId) {
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        requireOwner(workspace, userId);
        workspaceRepository.delete(workspace);
    }

    @Transactional
    public void invite(UUID workspaceId, InviteMemberRequest request, UUID inviterId) {
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        requireOwnerOrEditor(workspaceId, inviterId);

        // Editor는 VIEWER 권한만 초대 가능
        WorkspaceMember inviter = getMemberOrThrow(workspaceId, inviterId);
        if (inviter.getRole() == MemberRole.EDITOR && request.getRole() != MemberRole.VIEWER) {
            throw BusinessException.forbidden("Editors can only invite viewers");
        }

        User invitee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> BusinessException.notFound("User not found: " + request.getEmail()));

        if (memberRepository.existsByIdWorkspaceIdAndIdUserId(workspaceId, invitee.getId())) {
            throw BusinessException.conflict("User is already a member");
        }

        memberRepository.save(WorkspaceMember.of(workspaceId, invitee.getId(), request.getRole()));
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID targetUserId, UUID requesterId) {
        Workspace workspace = getWorkspaceOrThrow(workspaceId);
        requireOwner(workspace, requesterId);

        if (targetUserId.equals(requesterId)) {
            throw BusinessException.forbidden("Owner cannot remove themselves");
        }

        memberRepository.deleteByIdWorkspaceIdAndIdUserId(workspaceId, targetUserId);
    }

    // --- helpers ---

    private Workspace getWorkspaceOrThrow(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> BusinessException.notFound("Workspace not found"));
    }

    private WorkspaceMember getMemberOrThrow(UUID workspaceId, UUID userId) {
        return memberRepository.findByIdWorkspaceIdAndIdUserId(workspaceId, userId)
                .orElseThrow(() -> BusinessException.forbidden("Not a member of this workspace"));
    }

    private void requireOwner(Workspace workspace, UUID userId) {
        if (!workspace.getOwnerId().equals(userId)) {
            throw BusinessException.forbidden("Only the owner can perform this action");
        }
    }

    private void requireOwnerOrEditor(UUID workspaceId, UUID userId) {
        WorkspaceMember member = getMemberOrThrow(workspaceId, userId);
        if (member.getRole() == MemberRole.VIEWER) {
            throw BusinessException.forbidden("Viewers cannot perform this action");
        }
    }
}
