package cn.xuanyuanli.jdbc.pagination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableRequestTest {

    @Test
    void testConstructorWithParameters() {
        PageableRequest request = new PageableRequest(2, 20);
        assertEquals(2, request.getIndex());
        assertEquals(20, request.getSize());
    }

    @Test
    void testDefaultConstructor() {
        PageableRequest request = new PageableRequest();
        assertEquals(1, request.getIndex());
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
    }

    @Test
    void testSetIndex() {
        PageableRequest request = new PageableRequest();
        request.setIndex(0);
        assertEquals(1, request.getIndex());
        request.setIndex(-1);
        assertEquals(1, request.getIndex());
        request.setIndex(5);
        assertEquals(5, request.getIndex());
    }

    @Test
    void testGetSize() {
        PageableRequest request = new PageableRequest();
        request.setSize(0);
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
        request.setSize(-1);
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
        request.setSize(10);
        assertEquals(10, request.getSize());
    }

    @Test
    void testGetStart() {
        PageableRequest request = new PageableRequest();
        request.setStart(10);
        assertEquals(10, request.getStart());
    }

    @Test
    void testNewPageable() {
        PageableRequest request = new PageableRequest(2, 20);
        request.setStart(10);
        request.setTotalElements(100);
        Pageable<?> pageable = request.newPageable();
        assertEquals(2, pageable.getIndex());
        assertEquals(20, pageable.getSize());
        assertEquals(10, pageable.getStart());
        assertEquals(100, pageable.getTotalElements());
    }

    @Test
    void testBuildPageRequestWithNull() {
        PageableRequest request = PageableRequest.buildPageRequest(null);
        assertEquals(1, request.getIndex());
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
    }

    @Test
    void testBuildPageRequestWithInvalidValues() {
        PageableRequest input = new PageableRequest(0, 0);
        PageableRequest request = PageableRequest.buildPageRequest(input);
        assertEquals(1, request.getIndex());
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
    }

    @Test
    void testBuildPageRequestWithNegativeValues() {
        PageableRequest input = new PageableRequest(-1, -10);
        PageableRequest request = PageableRequest.buildPageRequest(input);
        assertEquals(1, request.getIndex());
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
    }

    @Test
    void testBuildPageRequestWithValidValues() {
        PageableRequest input = new PageableRequest(2, 20);
        PageableRequest request = PageableRequest.buildPageRequest(input);
        assertEquals(2, request.getIndex());
        assertEquals(20, request.getSize());
    }

    @Test
    void testBuildPageRequestWithoutParameters() {
        PageableRequest request = PageableRequest.buildPageRequest();
        assertEquals(1, request.getIndex());
        assertEquals(Pageable.DEFAULT_SIZE, request.getSize());
    }
}
