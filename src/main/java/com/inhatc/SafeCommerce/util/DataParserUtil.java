package com.inhatc.SafeCommerce.util;

/**
 * 데이터 파싱 관련 유틸리티 클래스
 * 공통적으로 사용되는 데이터 변환 로직을 관리
 */
public class DataParserUtil {

    /**
     * Object를 Long 타입으로 변환
     *
     * @param value 변환할 값
     * @return 변환된 Long 값
     */
    public static Long parseLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        } else {
            throw new IllegalArgumentException("잘못된 데이터 타입입니다. Long이 필요합니다.");
        }
    }

    /**
     * Object를 int 타입으로 변환
     *
     * @param value 변환할 값
     * @return 변환된 int 값
     */
    public static int parseIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else {
            throw new IllegalArgumentException("잘못된 데이터 타입입니다. int가 필요합니다.");
        }
    }
}