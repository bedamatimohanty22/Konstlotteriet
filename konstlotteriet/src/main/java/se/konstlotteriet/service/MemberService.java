package se.konstlotteriet.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import se.konstlotteriet.domain.Member;
import se.konstlotteriet.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MemberService {

    @Inject
    MemberRepository memberRepository;

    public List<Member> getAllMembers() {
        return memberRepository.listAll();
    }

    public List<Member> getActiveMembers() {
        return memberRepository.findActiveMembers();
    }

    public Optional<Member> getMemberById(Long id) {
        return Optional.ofNullable(memberRepository.findById(id));
    }

    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Transactional
    public Member createMember(Member member) {
        // Check if email already exists
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new IllegalArgumentException("En medlem med denna e-postadress finns redan");
        }
        memberRepository.persist(member);
        return member;
    }

    @Transactional
    public Member updateMember(Long id, Member updatedMember) {
        Member existingMember = memberRepository.findById(id);
        if (existingMember == null) {
            throw new IllegalArgumentException("Medlem med ID " + id + " hittades inte");
        }

        // Check if email is being changed and if new email already exists
        if (!existingMember.getEmail().equals(updatedMember.getEmail())) {
            if (memberRepository.findByEmail(updatedMember.getEmail()).isPresent()) {
                throw new IllegalArgumentException("En annan medlem med denna e-postadress finns redan");
            }
        }

        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
        existingMember.setMembershipStartYear(updatedMember.getMembershipStartYear());
        existingMember.setLastWinYear(updatedMember.getLastWinYear());
        existingMember.setActive(updatedMember.getActive());

        return existingMember;
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + id + " hittades inte");
        }
        // Soft delete - just mark as inactive
        member.setActive(false);
    }

    @Transactional
    public void reactivateMember(Long id) {
        Member member = memberRepository.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("Medlem med ID " + id + " hittades inte");
        }
        member.setActive(true);
    }

    public long countActiveMembers() {
        return memberRepository.countActiveMembers();
    }
}
