package se.konstlotteriet.dto;

import se.konstlotteriet.domain.Member;

public class MemberDTO {

    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public Integer membershipStartYear;
    public Integer lastWinYear;
    public Boolean active;

    public MemberDTO() {
    }

    public static MemberDTO fromEntity(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.id = member.getId();
        dto.firstName = member.getFirstName();
        dto.lastName = member.getLastName();
        dto.email = member.getEmail();
        dto.phoneNumber = member.getPhoneNumber();
        dto.membershipStartYear = member.getMembershipStartYear();
        dto.lastWinYear = member.getLastWinYear();
        dto.active = member.getActive();
        return dto;
    }

    public Member toEntity() {
        Member member = new Member();
        member.setFirstName(this.firstName);
        member.setLastName(this.lastName);
        member.setEmail(this.email);
        member.setPhoneNumber(this.phoneNumber);
        member.setMembershipStartYear(this.membershipStartYear);
        member.setLastWinYear(this.lastWinYear);
        member.setActive(this.active != null ? this.active : true);
        return member;
    }
}
