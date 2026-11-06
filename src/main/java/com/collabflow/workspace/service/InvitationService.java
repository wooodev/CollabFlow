package com.collabflow.workspace.service;

import com.collabflow.auth.domain.User;
import com.collabflow.auth.repository.UserRepository;
import com.collabflow.common.exception.BusinessException;
import com.collabflow.common.mail.MailService;
import com.collabflow.workspace.domain.Invitation;
import com.collabflow.workspace.domain.Workspace;
import com.collabflow.workspace.domain.WorkspaceMember;
import com.collabflow.workspace.domain.WorkspaceMember.MemberRole;
import com.collabflow.workspace.dto.InviteMemberRequest;
import com.collabflow.workspace.dto.InvitationResponse;
import com.collabflow.workspace.repository.InvitationRepository;
import com.collabflow.workspace.repository.WorkspaceMemberRepository;
import com.collabflow.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.invitation-expiry-hours:72}")
    private int expiryHours;

    @Transactional
    public InvitationResponse invite(UUID workspaceId, InviteMemberRequest request, UUID inviterId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> BusinessException.notFound("Workspace not found"));

        requireOwnerOrEditor(workspaceId, inviterId, request.getRole());

        String email = request.getEmail();
        if (invitationRepository.existsByWorkspaceIdAndEmailAndAcceptedAtIsNull(workspaceId, email)) {
            throw BusinessException.conflict("Pending invitation already exists for " + email);
        }

        // 이미 멤버인 경우 확인
        userRepository.findByEmail(email).ifPresent(user -> {
            if (memberRepository.existsByIdWorkspaceIdAndIdUserId(workspaceId, user.getId())) {
                throw BusinessException.conflict("User is already a member");
            }
        });

        Invitation invitation = Invitation.create(workspaceId, email, request.getRole(), inviterId, expiryHours);
        invitationRepository.save(invitation);

        String inviteLink = frontendUrl + "/invite/" + invitation.getToken();
        mailService.sendInvitation(email, workspace.getName(), inviteLink);

        return InvitationResponse.from(invitation);
    }

    @Transactional(readOnly = true)
    public InvitationResponse getInvitation(UUID token) {
        Invitation invitation = getInvitationOrThrow(token);
        return InvitationResponse.from(invitation);
    }

    @Transactional
    public void accept(UUID token, UUID userId) {
        Invitation invitation = getInvitationOrThrow(token);

        if (invitation.isAccepted()) {
            throw BusinessException.conflict("Invitation has already been accepted");
        }
        if (invitation.isExpired()) {
            throw BusinessException.conflict("Invitation has expired");
        }

        // 초대된 이메일과 수락하는 유저의 이메일이 일치하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        if (!user.getEmail().equalsIgnoreCase(invitation.getEmail())) {
            throw BusinessException.forbidden("This invitation was sent to a different email address");
        }

        if (memberRepository.existsByIdWorkspaceIdAndIdUserId(invitation.getWorkspaceId(), userId)) {
            throw BusinessException.conflict("Already a member of this workspace");
        }

        memberRepository.save(WorkspaceMember.of(invitation.getWorkspaceId(), userId, invitation.getRole()));
        invitation.accept();
    }

    private Invitation getInvitationOrThrow(UUID token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> BusinessException.notFound("Invitation not found"));
    }

    private void requireOwnerOrEditor(UUID workspaceId, UUID userId, MemberRole targetRole) {
        WorkspaceMember member = memberRepository.findByIdWorkspaceIdAndIdUserId(workspaceId, userId)
                .orElseThrow(() -> BusinessException.forbidden("Not a member of this workspace"));
        if (member.getRole() == MemberRole.VIEWER) {
            throw BusinessException.forbidden("Viewers cannot invite members");
        }
        if (member.getRole() == MemberRole.EDITOR && targetRole != MemberRole.VIEWER) {
            throw BusinessException.forbidden("Editors can only invite viewers");
        }
    }
}
