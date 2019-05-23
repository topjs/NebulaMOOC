/*
 * @author Zhanghh
 * @date 2019/5/19
 */
package com.nebula.mooc.webserver.service.impl;

import com.nebula.mooc.core.Constant;
import com.nebula.mooc.core.entity.*;
import com.nebula.mooc.webserver.dao.CourseDao;
import com.nebula.mooc.webserver.dao.VideoDao;
import com.nebula.mooc.webserver.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("CourseService")
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseDao courseDao;

    @Autowired
    private VideoDao videoDao;

    public int getCourseListTotal(String kindName) {
        return courseDao.getCourseTotal(kindName);
    }

    public List getCourseList(String kindName, int offset) {
        return courseDao.getCourseList(kindName, offset, Constant.PAGE_SIZE);
    }

    public Course getCourse(long userId, long courseId) {
        Course course = courseDao.getCourse(userId, courseId);
        if (course == null) return null;
        // 获取课程里的章列表
        List<CourseChapter> chapterList = courseDao.getCourseChapterList(courseId);
        if (chapterList == null) return course;
        course.setChapterList(chapterList);
        // 获取课程里的节列表
        for (CourseChapter chapter : chapterList) {
            List<CourseSection> sectionList = courseDao.getCourseSectionList(chapter.getId());
            chapter.setSectionList(sectionList);
        }
        return course;
    }

    public int getCourseCommentTotal(long courseId) {
        return courseDao.getCourseCommentTotal(courseId);
    }

    public List getCourseCommentList(long userId, long courseId, int offset) {
        return courseDao.getCourseCommentList(userId, courseId, offset, Constant.PAGE_SIZE);
    }

    public CourseSection getCourseSection(long sectionId) {
        return courseDao.getCourseSection(sectionId);
    }

    public int getCourseSectionCommentTotal(long sectionId) {
        return courseDao.getCourseSectionCommentTotal(sectionId);
    }

    public List<CourseSectionComment> getCourseSectionCommentList(long userId, long sectionId, int offset) {
        List<CourseSectionComment> commentList = courseDao.getCourseSectionCommentList(userId, sectionId, offset, Constant.PAGE_SIZE);
        if (commentList == null) return null;
        for (CourseSectionComment comment : commentList) {
            List<List<CourseSectionCommentReply>> commentReply = new ArrayList<>();
            commentReply.add(courseDao.getCourseSectionCommentReplyList(comment.getId()));
            comment.setReply(commentReply);
        }
        return commentList;
    }

    public List getHomeCourseList() {
        List<List> courseList = new ArrayList<>(10);
        Object[] kindNames = Constant.KIND_MAP.values().toArray();
        for (int i = 1; i <= 10; i++) {
            String kindName = (String) kindNames[i];
            courseList.add(courseDao.getCourseList(kindName, 0, 4));
        }
        return courseList;
    }

    @Transactional
    public boolean newCourse(Course course) {
        int result = courseDao.newCourse(course);
        if (result != 1) return false;
        long courseId = courseDao.getLastInsertId();
        Video video = new Video();
        video.setUserId(course.getUserId());
        for (CourseChapter chapter : course.getChapterList()) {
            chapter.setCourseId(courseId);
            result = courseDao.newCourseChapter(chapter);
            if (result != 1) return false;
            long chapterId = courseDao.getLastInsertId();
            List<CourseSection> courseSectionList = chapter.getSectionList();
            for (CourseSection section : courseSectionList) {
                section.setChapterId(chapterId);
                result = courseDao.newCourseSection(section);
                if (result != 1) return false;
                video.setFilename(section.getUrl());
                videoDao.removeVideo(video);
            }
        }
        result = courseDao.increaseNum(course.getKindName());
        return result == 2;
    }

    @Transactional
    public boolean updateCourse(Course course) {
        int result = courseDao.updateCourse(course);
        if (result != 1) return false;
        long courseId = course.getId();
        for (CourseChapter chapter : course.getChapterList()) {
            chapter.setCourseId(courseId);
            result = courseDao.updateCourseChapter(chapter);
            if (result != 1) return false;
            long chapterId = chapter.getId();
            List<CourseSection> courseSectionList = chapter.getSectionList();
            for (CourseSection section : courseSectionList) {
                section.setChapterId(chapterId);
                result = courseDao.updateCourseSection(section);
                if (result != 1) return false;
            }
        }
        return true;
    }

    @Override
    public boolean ifStar(Course course) {
        return courseDao.ifStar(course.getUserId(), course.getId()) > 0;
    }

    @Override
    public boolean courseStar(Course course) {
        return courseDao.courseStar(course.getUserId(), course.getId()) + courseDao.addCourseStar(course.getId()) > 1;
    }

    @Override
    public boolean delCourseStar(Course course) {
        return courseDao.delCourseStar(course.getUserId(), course.getId()) + courseDao.subCourseStar(course.getId()) > 1;
    }

    @Override
    public boolean ifLike(Course course) {
        return courseDao.ifLike(course.getUserId(), course.getId()) > 0;
    }

    @Override
    public boolean courseLike(Course course) {
        return courseDao.courseLike(course.getUserId(), course.getId()) + courseDao.addCourseLike(course.getId()) > 1;
    }

    @Override
    public boolean delCourseLike(Course course) {
        return courseDao.delCourseLike(course.getUserId(), course.getId()) + courseDao.subCourseLike(course.getId()) > 1;
    }

    @Override
    public boolean courseComment(CourseComment courseComment) {
        return courseDao.courseComment(courseComment.getUserId(), courseComment.getCourseId(), courseComment.getContent()) > 0;
    }

    @Override
    public boolean delCourseComment(CourseComment courseComment) {
        return courseDao.delCourseComment(courseComment.getUserId(), courseComment.getCourseId()) > 0;
    }

    @Override
    public boolean ifCourseCommentStar(CourseComment courseComment) {
        return courseDao.ifCourseCommentStar(courseComment.getUserId(), courseComment.getId()) > 0;
    }

    @Override
    public boolean courseCommentStar(CourseComment courseComment) {
        return courseDao.courseCommentStar(courseComment.getUserId(), courseComment.getId()) + courseDao.addCourseCommentStar(courseComment.getId()) > 1;
    }

    @Override
    public boolean delCourseCommentStar(CourseComment courseComment) {
        return courseDao.delCourseCommentStar(courseComment.getUserId(), courseComment.getId()) + courseDao.subCourseCommentStar(courseComment.getId()) > 1;
    }

    @Override
    public boolean sectionComment(CourseSectionComment courseSectionComment) {
        return courseDao.sectionComment(courseSectionComment.getUserId(), courseSectionComment.getSectionId(), courseSectionComment.getContent()) > 0;
    }

    @Override
    public boolean delSectionComment(CourseSectionComment courseSectionComment) {
        return courseDao.delSectionCommentStar(courseSectionComment.getUserId(), courseSectionComment.getId()) > 0;
    }

    @Override
    public boolean ifSectionCommentStar(CourseSectionComment courseSectionComment) {
        return courseDao.ifSectionCommentStar(courseSectionComment.getUserId(), courseSectionComment.getId()) > 0;
    }

    @Override
    public boolean sectionCommentStar(CourseSectionComment courseSectionComment) {
        return courseDao.sectionCommentStar(courseSectionComment.getUserId(), courseSectionComment.getId()) + courseDao.addSectionCommentStar(courseSectionComment.getId()) > 1;
    }

    @Override
    public boolean delSectionCommentStar(CourseSectionComment courseSectionComment) {
        return courseDao.delSectionCommentStar(courseSectionComment.getUserId(), courseSectionComment.getId()) + courseDao.subSectionCommentStar(courseSectionComment.getId()) > 1;
    }

    @Override
    public boolean sectionCommentReply(CourseSectionCommentReply courseSectionCommentReply) {
        return courseDao.sectionCommentReply(courseSectionCommentReply.getCommentId(), courseSectionCommentReply.getContent(), courseSectionCommentReply.getFromId(), courseSectionCommentReply.getToId()) > 0;
    }

    @Override
    public boolean delSectionCommentReply(CourseSectionCommentReply courseSectionCommentReply) {
        return courseDao.delSectionCommentReply(courseSectionCommentReply.getId(), courseSectionCommentReply.getFromId()) > 0;
    }

    @Override
    public long lastReplyId() {
        return courseDao.lastReplyId();
    }


}
