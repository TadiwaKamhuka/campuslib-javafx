package com.team.campuslib.service;

import com.team.campuslib.model.Member;
import com.team.campuslib.repository.MemberRepository;
import com.team.campuslib.repository.RepositoryFactory;
import com.team.campuslib.util.Transactions;
import com.team.campuslib.observer.DataChangePublisher;
import com.team.campuslib.observer.DataChangeType;
import com.team.campuslib.util.SessionManager;
import com.team.campuslib.util.ValidationHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MemberService {

    public List<Member> getAllMembers() {
        SessionManager.getInstance().requireAdmin();
        return Transactions.read(em ->
                RepositoryFactory.createMemberRepository(em).findAll());
    }

    public Optional<Member> getMemberById(Long id) {
        SessionManager.getInstance().requireAdmin();
        requireValid(ValidationHelper.validateMemberId(id));

        return Transactions.read(em ->
                RepositoryFactory.createMemberRepository(em).findById(id));
    }

    public List<Member> searchMembers(String query) {
        SessionManager.getInstance().requireAdmin();
        if (query == null || query.isBlank()) {
            return getAllMembers();
        }

        return Transactions.read(em ->
                RepositoryFactory.createMemberRepository(em).search(query));
    }

    public void addMember(Member member) {
        SessionManager.getInstance().requireAdmin();
        normalise(member);
        requireValid(ValidationHelper.validateMember(member));

        Transactions.write(em -> {
            MemberRepository repository =
                    RepositoryFactory.createMemberRepository(em);

            if (repository.existsByEmail(member.getEmail(), null)) {
                throw new IllegalArgumentException(
                        "A member with this email already exists.");
            }

            if (member.getRegistrationDate() == null) {
                member.setRegistrationDate(LocalDate.now());
            }

            member.setActive(true);
            repository.save(member);
            return null;
        });

        DataChangePublisher.publish(DataChangeType.MEMBERS);
    }

    public Member updateMember(Member member) {
        SessionManager.getInstance().requireAdmin();
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException(
                    "Member and member ID must not be null.");
        }

        normalise(member);
        requireValid(ValidationHelper.validateMember(member));

        Member updated = Transactions.write(em -> {
            MemberRepository repository =
                    RepositoryFactory.createMemberRepository(em);

            Member existing = repository.findById(member.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Member not found with ID: " + member.getId()));

            if (repository.existsByEmail(member.getEmail(), member.getId())) {
                throw new IllegalArgumentException(
                        "A member with this email already exists.");
            }

            member.setRegistrationDate(existing.getRegistrationDate());
            member.setActive(existing.isActive());

            return repository.update(member);
        });

        DataChangePublisher.publish(DataChangeType.MEMBERS);
        return updated;
    }

    public void deactivateMember(Long id) {
        SessionManager.getInstance().requireAdmin();
        setActive(id, false);
    }

    public void activateMember(Long id) {
        SessionManager.getInstance().requireAdmin();
        setActive(id, true);
    }

    public void deleteMember(Long id) {
        SessionManager.getInstance().requireAdmin();
        requireValid(ValidationHelper.validateMemberId(id));

        Transactions.write(em -> {
            MemberRepository repository =
                    RepositoryFactory.createMemberRepository(em);

            if (repository.findById(id).isEmpty()) {
                throw new IllegalArgumentException(
                        "Member not found with ID: " + id);
            }

            if (repository.countOpenLoans(id) > 0) {
                throw new IllegalStateException(
                        "This member has active loans and cannot be deleted. "
                                + "Deactivate the member instead.");
            }

            if (repository.countLoans(id) > 0) {
                throw new IllegalStateException(
                        "This member has loan history and cannot be deleted. "
                                + "Deactivate the member instead.");
            }

            repository.deleteById(id);
            return null;
        });

        DataChangePublisher.publish(DataChangeType.MEMBERS);
    }

    private void setActive(Long id, boolean active) {
        requireValid(ValidationHelper.validateMemberId(id));

        Transactions.write(em -> {
            Member member = RepositoryFactory.createMemberRepository(em)
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Member not found with ID: " + id));

            member.setActive(active);
            return null;
        });

        DataChangePublisher.publish(DataChangeType.MEMBERS);
    }

    private static void normalise(Member member) {
        if (member == null) {
            return;
        }

        if (member.getFullName() != null) {
            member.setFullName(member.getFullName().trim());
        }

        if (member.getEmail() != null) {
            member.setEmail(member.getEmail().trim().toLowerCase());
        }

        String phone = member.getPhone();
        member.setPhone(
                (phone == null || phone.isBlank()) ? null : phone.trim());
    }

    private static void requireValid(String error) {
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
    }
}
