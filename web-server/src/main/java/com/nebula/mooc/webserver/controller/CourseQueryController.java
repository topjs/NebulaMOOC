/*
 * @author Zhanghh
 * @date 2019/5/19
 */
package com.nebula.mooc.webserver.controller;

import com.nebula.mooc.core.Constant;
import com.nebula.mooc.core.entity.Return;
import com.nebula.mooc.core.entity.UserInfo;
import com.nebula.mooc.webserver.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/sys/course/")
public class CourseQueryController {

    @Autowired
    private CourseService courseService;

    private long getUserId(HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute(Constant.USERINFO);
        if (userInfo != null) return userInfo.getId();
        else return 0;
    }

    @GetMapping(value = "getHomeCourseList")
    public Return getCourseList() {
        return new Return(courseService.getHomeCourseList());
    }

    @PostMapping(value = "getCourseList")
    public Return getCourseList(int pageIndex, int kind) {
        if (pageIndex <= 0 || kind < 0 || kind > 10) return new Return(Constant.CLIENT_ERROR_CODE, "参数错误！");
        String kindName = Constant.KIND_MAP.get(kind);      // 获取类型名
        int total = courseService.getCourseListTotal(kindName);     // 总数
        int offset = (pageIndex - 1) * Constant.PAGE_SIZE;  // 偏移量
        Return ret = new Return<List>();
        // 如果总数为0或者偏移量过大
        if (total == 0 || offset > total) {
            // 设置总数为0，放在Return的Msg中
            ret.setMsg(String.valueOf(0));
            return ret;
        }
        ret.setMsg(String.valueOf(total));
        ret.setData(courseService.getCourseList(kindName, offset));
        return ret;
    }

    @PostMapping(value = "getCourse")
    public Return getCourse(HttpServletRequest request, long courseId) {
        if (courseId <= 0) return new Return(Constant.CLIENT_ERROR_CODE, "参数错误！");
        return new Return(courseService.getCourse(getUserId(request), courseId));
    }

    @PostMapping(value = "getCourseCommentList")
    public Return getCourseCommentList(HttpServletRequest request, long courseId, int pageIndex) {
        if (courseId <= 0 || pageIndex <= 0) return new Return(Constant.CLIENT_ERROR_CODE, "参数错误！");
        int total = courseService.getCourseCommentTotal(courseId);     // 总数
        int offset = (pageIndex - 1) * Constant.PAGE_SIZE;  // 偏移量
        Return ret = new Return<List>();
        // 如果总数为0或者偏移量过大
        if (total == 0 || offset > total) {
            // 设置总数为0，放在Return的Msg中
            ret.setMsg(String.valueOf(0));
            return ret;
        }
        ret.setMsg(String.valueOf(total));
        ret.setData(courseService.getCourseCommentList(getUserId(request), courseId, offset));
        return ret;
    }

    @PostMapping(value = "getCourseSection")
    public Return getCourseSection(long sectionId) {
        if (sectionId <= 0) return new Return(Constant.CLIENT_ERROR_CODE, "参数错误！");
        return new Return(courseService.getCourseSection(sectionId));
    }

    @PostMapping(value = "getCourseSectionCommentList")
    public Return getCourseSectionCommentList(HttpServletRequest request, long sectionId, int pageIndex) {
        if (sectionId <= 0 || pageIndex <= 0) return new Return(Constant.CLIENT_ERROR_CODE, "参数错误！");
        int total = courseService.getCourseSectionCommentTotal(sectionId);     // 总数
        int offset = (pageIndex - 1) * Constant.PAGE_SIZE;  // 偏移量
        Return ret = new Return<List>();
        // 如果总数为0或者偏移量过大
        if (total == 0 || offset > total) {
            // 设置总数为0，放在Return的Msg中
            ret.setMsg(String.valueOf(0));
            return ret;
        }
        ret.setMsg(String.valueOf(total));
        ret.setData(courseService.getCourseSectionCommentList(getUserId(request), sectionId, offset));
        return ret;
    }
}
