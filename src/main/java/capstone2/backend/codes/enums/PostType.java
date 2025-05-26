package capstone2.backend.codes.enums;

public enum PostType {

    GENERAL(0, "일반"),              // 동아리 내부 공통 텍스트 게시글
    NOTICE(1, "공지"),
    SCHEDULE(2, "일정"),
    VOTE(3, "투표"),
    POSTER(4, "홍보 포스터"),
    AOC(5, "총학생회");

    private final int code;
    private final String label;

    PostType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PostType fromCode(int code) {
        for (PostType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("존재하지 않는 코드: " + code);
    }
}
