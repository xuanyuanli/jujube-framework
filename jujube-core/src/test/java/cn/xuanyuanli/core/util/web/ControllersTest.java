package cn.xuanyuanli.core.util.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ControllersTest {

    @Test
    void redirect() {
        assertEquals(Controllers.redirect("a"),"redirect:a");
        assertEquals(Controllers.redirect("/a"),"redirect:/a");
    }

}
