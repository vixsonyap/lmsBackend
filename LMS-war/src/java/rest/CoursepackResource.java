/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import datamodel.rest.CreateCoursepack;
import datamodel.rest.ErrorRsp;
import datamodel.rest.GetCategoryRsp;
import datamodel.rest.GetCoursepackRsp;
import datamodel.rest.UpdateCoursepack;
import datamodel.rest.GetUserRsp;
import entities.Category;
import entities.Coursepack;
import entities.File;
import entities.ForumTopic;
import entities.LessonOrder;
import entities.Outlines;
import entities.Quiz;
import entities.Rating;
import entities.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import util.AccessRightEnum;
import util.LessonOrderStatusEnum;

/**
 *
 *
 */
@Stateless
@Path("Coursepack")
public class CoursepackResource {

    @PersistenceContext(unitName = "LMS-warPU")
    private EntityManager em;

    public CoursepackResource() {

    }

    public boolean isLogin(User user) {
        user = em.find(User.class, user.getId());
        if (user != null) {
            return true;
        }
        return false;
    }

    public User userLogin(String username, String password) {
        User user = em.find(User.class, username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        throw new NotFoundException("Username does not exist");
    }

    @PUT
    @Path(value = "createCoursepack")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCoursepack(CreateCoursepack createCoursepack, @QueryParam("userId") Long userId) {
        try {
            Coursepack coursepack = new Coursepack();
            coursepack.setCode(createCoursepack.getCode());
            coursepack.setTitle(createCoursepack.getTitle());
            coursepack.setDescription(createCoursepack.getDescription());
            //coursepack.setCategory(createCoursepack.getCategory());
            if (createCoursepack.getImageLocation() != null && createCoursepack.getImageLocation().length() > 0) {
                coursepack.setImageLocation(createCoursepack.getImageLocation());
            }
            else {
                coursepack.setImageLocation("https://icon-library.net/images/no-image-available-icon/no-image-available-icon-6.jpg");
            }
            
            Category category = em.find(Category.class, createCoursepack.getCategoryId());
            if (category == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Category does not exist").build();
            }
            coursepack.setPrice(createCoursepack.getPrice());
            coursepack.setTeacherBackground(createCoursepack.getTeacherBackground());
            coursepack.setRating(0.00);
            em.persist(coursepack);
            em.flush();
            
            coursepack.setCategory(category);
            category.getCoursepackList().add(coursepack);
            em.flush();

            User user = em.find(User.class, userId);
            User userCopy = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
            coursepack.setAssignedTeacher(user);
            user.getTeacherCoursepackList().add(coursepack);

            Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                    coursepack.getDescription(), coursepack.getPrice(), null, null,
                    coursepack.getImageLocation(), null,
                    coursepack.getTeacherBackground(), null, null, null, userCopy, null, null, null, null);

            return Response.status(Response.Status.OK).entity(coursepackCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path(value = "createOutline")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOutline(@QueryParam("coursepackId") Long coursepackId, @QueryParam("name") String name) {
        try {

            Coursepack cp = em.find(Coursepack.class, coursepackId);
            if (cp == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
            }

            Outlines outlines = new Outlines();
            outlines.setName(name);
            outlines.setNumber(cp.getOutlineList().size() + 1);
            outlines.setCoursepack(cp);
            em.persist(outlines);
            cp.getOutlineList().add(outlines);
            em.flush();

//            Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
//                        coursepack.getDescription(), coursepack.getCategory(), coursepack.getPrice(), null, null, 
//                        coursepack.getTeacherBackground(), null, null, null, null, null, outlines);
//        
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "deleteOutline")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteOutline(@QueryParam("outlineId") Long outlineId) {
        Outlines outline = em.find(Outlines.class, outlineId);
        if (outline == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Outline does not exist").build();
        }
        List<LessonOrder> lessonOrderList = outline.getLessonOrder();
        if (lessonOrderList.isEmpty() || lessonOrderList == null) {
            outline.getCoursepack().getOutlineList().remove(outline);
        }
        for (LessonOrder lo : lessonOrderList) {
            File file = lo.getFile();
            if (file != null) {
                file.setLessonOrder(null);
                lo.setFile(null);
//                em.flush();
            }
            Quiz quiz = lo.getQuiz();
            if (quiz != null) {
                quiz.setLessonOrder(null);
                lo.setQuiz(null);
//                em.flush();
            }
//            List<User> userList = lo.getPublicUserList();
//            if (!userList.isEmpty() || userList != null) {
//                for (User u : userList) {
//                    lo.getPublicUserList().remove(u);
//                }
//                em.flush();
//            }
            lo.setOutlines(null);
//            outline.getLessonOrder().remove(lo);
        }
        outline.setLessonOrder(null);
        outline.getCoursepack().getOutlineList().remove(outline);
        em.remove(outline);
        return Response.status(Response.Status.OK).build();
    }

    @Path(value = "updateOutline")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOutline(@QueryParam("outlineId") Long outlineId, @QueryParam("name") String name) {

        try {

            Outlines o = em.find(Outlines.class, outlineId);
            if (o != null) {

                o.setName(name);

                Coursepack cp = o.getCoursepack();

                Coursepack coursepackCopy = new Coursepack(cp.getCoursepackId(), cp.getCode(), cp.getTitle(),
                        cp.getDescription(), cp.getPrice(), null, null,
                        cp.getImageLocation(), null,
                        cp.getTeacherBackground(), null, null,
                        null, null, null, null, null, null);

                Outlines outline = new Outlines(o.getOutlineId(), coursepackCopy, null, o.getName(), o.getNumber());

                return Response.status(Response.Status.OK).entity(outline).build();

            }
            return Response.status(Response.Status.NOT_FOUND).entity("Outline does not exist").build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

//    @Path(value = "updateOutline")
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateOutline(@QueryParam("coursepackId") Long coursepackId,
//                                        @QueryParam("outlineId") Long outlineId, @QueryParam("name") String name){
//
//      try{
//          Coursepack cp = em.find(Coursepack.class, coursepackId);
//            if(cp ==null){
//                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
//            }
//            if(cp.getOutlineList().isEmpty()){
//                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
//            }else{
//          
//          Outlines o = em.find(Outlines.class, outlineId);
//          if(o !=null){
//              o.setName(name);
//              
//              //Coursepack cp = o.getCoursepack();
//              User user = o.getCoursepack().getAssignedTeacher();
//              em.merge(o);
//              em.flush();
//              
//              User assignedTeacher = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
//                    user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
//              
//              Coursepack coursepackCopy = new Coursepack(cp.getCoursepackId(), cp.getCode(), cp.getTitle(),
//                        cp.getDescription(), cp.getCategory(), cp.getPrice(), null, null, 
//                        cp.getTeacherBackground(), null, null, 
//                        null, assignedTeacher, null, null);
//              
//              Outlines outlinesCopy = new Outlines(o.getOutlineId(), coursepackCopy, o.getLessonOrder(), o.getName(), o.getNumber());
//              return Response.status(Response.Status.OK).entity(outlinesCopy).build();
//          }
//            }
//              return Response.status(Response.Status.NOT_FOUND).entity("Outlines does not exist").build();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//       
//    }
    @Path(value = "swapOutline")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response swapOutline(@QueryParam("outlineId1") Long outlineId1, @QueryParam("outlineId2") Long outlineId2) {
        try {
            Outlines o1 = em.find(Outlines.class, outlineId1);

            if (o1 == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Outlines does not exist").build();
            }
            Outlines o2 = em.find(Outlines.class, outlineId2);

            if (o2 == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Outlines does not exist").build();
            }

            int temp = o2.getNumber();
            o2.setNumber(o1.getNumber());
            o1.setNumber(temp);

            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

//    @GET
//    @Path(value = "getAllOutlineForCoursepack")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getAllOutlineForCoursepack(@QueryParam("coursepackId") Long coursepackId) {
//        try {
//            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
//            if (coursepack == null) {
//                return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();
//            }
//            
//            GetOutlineRsp rsp = new GetOutlineRsp(new ArrayList<>());
//            List <Outlines> outlines = coursepack.getOutlineList();
//            if(outlines == null && outlines.isEmpty()){
//                return Response.status(Response.Status.NOT_FOUND).entity("No outlines found").build();
//            }else{
//                for(Outlines outline: outlines){
//                    List<LessonOrder> lessonOrder = outline.getLessonOrder();
//                    for(LessonOrder lo : lessonOrder){
//                        lo.getFile();
//                        lo.getQuiz();
//                        lo.getNumber();
//                        lo.getName();
//                        lessonOrder.add(lo);
//                    }
//                    rsp.getOutlines().add(new Outlines(outline.getOutlineId(), 
//                            outline.getCoursepack(), lessonOrder, outline.getName(), outline.getNumber()));
//                }
//            }
//            return Response.status(Response.Status.OK).entity(rsp).build();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    //LessonOrder
    @PUT
    @Path(value = "createLessonOrder")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLessonOrder(@QueryParam("outlineId") Long outlineId, @QueryParam("name") String name, @QueryParam("type") String type, @QueryParam("id") Long id) {
        try {

            Outlines outline = em.find(Outlines.class, outlineId);
            if (outline == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Outline does not exist").build();
            }

            LessonOrder lo = new LessonOrder();
            lo.setName(name);
            lo.setNumber(outline.getLessonOrder().size() + 1);
            lo.setOutlines(outline);

            if (type.contains("quiz")) {
                Quiz quiz = em.find(Quiz.class, id);
                if (quiz == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Quiz does not exist").build();
                }
                lo.setQuiz(quiz);
                quiz.setLessonOrder(lo);
            } else if (type.contains("file")) {
                File file = em.find(File.class, id);
                if (file == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("File does not exist").build();
                }
                lo.setFile(file);
                file.setLessonOrder(lo);
            }
            em.persist(lo);
            outline.getLessonOrder().add(lo);
            em.flush();

            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "deleteLessonOrder")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLessonOrder(@QueryParam("lessonOrderId") Long lessonOrderId) {

        LessonOrder lessonOrder = em.find(LessonOrder.class, lessonOrderId);
        if (lessonOrder == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Lesson order does not exist").build();
        }

        if (lessonOrder.getFile() != null) {
            File file = lessonOrder.getFile();

            file.setLessonOrder(null);
            lessonOrder.setFile(null);
            em.merge(file);
        }

        if (lessonOrder.getQuiz() != null) {
            Quiz quiz = lessonOrder.getQuiz();
            quiz.setLessonOrder(null);
            lessonOrder.setQuiz(null);
            em.merge(quiz);
        }
        em.flush();

        lessonOrder.getOutlines().getLessonOrder().remove(lessonOrder);
        em.remove(lessonOrder);

        return Response.status(Response.Status.OK).build();
    }

    @Path(value = "updateLessonOrder")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateLessonOrder(@QueryParam("lessonOrderId") Long lessonOrderId, @QueryParam("name") String name) {
        try {

            LessonOrder lo = em.find(LessonOrder.class, lessonOrderId);
            if (lo != null) {
                lo.setName(name);

                LessonOrder lessonOrder = new LessonOrder(lo.getLessonOrderId(), lo.getNumber(), lo.getName(), null, null, null, null, null);

                return Response.status(Response.Status.OK).entity(lessonOrder).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Lesson Order does not exist").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Path(value = "swapLessonOrder")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response swapLessonOrder(@QueryParam("lessonOrderId1") Long lessonOrderId1, @QueryParam("lessonOrderId2") Long lessonOrderId2) {
        try {
            LessonOrder o1 = em.find(LessonOrder.class, lessonOrderId1);
            if (o1 == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Lesson order does not exist").build();
            }
            LessonOrder o2 = em.find(LessonOrder.class, lessonOrderId2);
            if (o2 == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Lesson order does not exist").build();
            }
            int temp = o2.getNumber();
            o2.setNumber(o1.getNumber());
            o1.setNumber(temp);

            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @Path(value = "getLessonOrderByOutlineId/{outlineId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLessonOrderByOutlineId(@PathParam("outlineId") Long outlineId) {
        try {

            Outlines outline = em.find(Outlines.class, outlineId);

            if (outline == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Outline does not exist").build();
            }

            Outlines newOut = new Outlines();

            List<LessonOrder> lessonOrder = new ArrayList<>();
            for (LessonOrder lo : outline.getLessonOrder()) {
                LessonOrder nlo = new LessonOrder();
                nlo.setName(lo.getName());
                nlo.setNumber(lo.getNumber());
                nlo.setLessonOrderId(lo.getLessonOrderId());
                if (lo.getQuiz() != null) {
                    Quiz quizCopy = new Quiz();
                    quizCopy.setTitle(lo.getQuiz().getTitle());
                    quizCopy.setQuizId(lo.getQuiz().getQuizId());

                    nlo.setQuiz(quizCopy);
                }

                if (lo.getFile() != null) {
                    File fileCopy = new File();
                    fileCopy.setName(lo.getFile().getName());
                    fileCopy.setFileId(lo.getFile().getFileId());

                    nlo.setFile(fileCopy);
                }

                lessonOrder.add(nlo);
            }
            newOut.setLessonOrder(lessonOrder);
//              oline.add(newOut);

            return Response.status(Response.Status.OK).entity(newOut).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "deleteCoursepack")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCoursepack(@QueryParam("coursepackId") Long coursepackId) {

        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();
            }

////            coursepack.getQuizList()
////            coursepack.setQuizList(null);
//            coursepack.getAssignedTeacher().getTeacherCoursepackList().remove(coursepack);
//            coursepack.setAssignedTeacher(null);
//            coursepack.setOutlineList(outlineList);
            for (ForumTopic ft : coursepack.getForumTopicList()) {
                ft.setCoursepack(null);
            }
            em.remove(coursepack);

            return Response.status(Response.Status.OK).entity("Coursepack deleted").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "deleteCoursepackByCode")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCoursepackByCode(@QueryParam("code") String code) {
        try {
            Query query = em.createQuery("select cp from Coursepack cp where cp.code =:code");
            query.setParameter("code", code);
            Coursepack coursepack = (Coursepack) query.getSingleResult();

            if (coursepack != null) {
                em.remove(coursepack);
                return Response.status(Response.Status.OK).entity(coursepack).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Path(value = "updateCoursepack")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCoursepack(UpdateCoursepack updateCoursepack, @QueryParam("coursepackId") Long coursepackId) {
        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack != null) {
                coursepack.setCode(updateCoursepack.getCode());
                coursepack.setTitle(updateCoursepack.getTitle());
                coursepack.setDescription(updateCoursepack.getDescription());
                //coursepack.setCategory(updateCoursepack.getCategory());

                if (updateCoursepack.getImageLocation() != null && updateCoursepack.getImageLocation().length() > 0) {
                    coursepack.setImageLocation(updateCoursepack.getImageLocation());
                } else {
                    coursepack.setImageLocation("https://icon-library.net/images/no-image-available-icon/no-image-available-icon-6.jpg");
                }

                Category category = em.find(Category.class, updateCoursepack.getCategoryId());
                if (category == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Category does not exist").build();
                }

                coursepack.setPrice(updateCoursepack.getPrice());
                coursepack.setPublished(updateCoursepack.getPublished());
                coursepack.setTeacherBackground(updateCoursepack.getTeacherBackground());
                //coursepack.setAssignedTeacher(updateCoursepack.getUser());
                //User user = em.find(User.class, userId);
                User user = coursepack.getAssignedTeacher();
//                User userCopy = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
//                    user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
                //coursepack.setAssignedTeacher(user);
                coursepack.setCategory(category);
                category.getCoursepackList().add(coursepack);
                em.merge(coursepack);
                em.flush();

                User assignedTeacher = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                        user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);

                Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                        coursepack.getDescription(), coursepack.getPrice(), null, null,
                        coursepack.getImageLocation(), null,
                        coursepack.getTeacherBackground(), null, null,
                        null, assignedTeacher, null, null, null, null);

                return Response.status(Response.Status.OK).entity(coursepackCopy).build();

            }
            return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "updateCoursepackDescription/{coursepackId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCoursepackDescripton(String description, @PathParam("coursepackId") Long coursepackId) {
        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
            }

            coursepack.setDescription(description);
            em.flush();
            return Response.status(Response.Status.OK).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path(value = "getAllPublicUserByCoursepack")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStudentByCoursepack(@QueryParam("coursepackId") Long coursepackId) {
        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();
            }
            GetUserRsp rsp = new GetUserRsp(new ArrayList<>());
            List<User> publicUser = coursepack.getPublicUserList();
            if (publicUser == null && publicUser.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No public user found").build();
            }

            for (User pu : publicUser) {
                rsp.getUserList().add(
                        new User(null, pu.getUserId(), pu.getFirstName(), pu.getLastName(), pu.getEmail(),
                                pu.getUsername(), null, pu.getGender(), pu.getAccessRight(), null, pu.getQuizAttemptList(),
                                null, null, null, null, pu.getPublicUserModuleList())
                );
            }
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getCoursepack/{coursepackId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoursepackById(@PathParam("coursepackId") Long coursepackId) {
        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
            }

            List<Outlines> oline = new ArrayList<>();
            for (Outlines o : coursepack.getOutlineList()) {
                //add in lesson order (Loop inside loop)
                Outlines newOut = new Outlines();
                newOut.setName(o.getName());
                newOut.setNumber(o.getNumber());
                newOut.setOutlineId(o.getOutlineId());

                List<LessonOrder> lessonOrder = new ArrayList<>();
                for (LessonOrder lo : o.getLessonOrder()) {
                    LessonOrder nlo = new LessonOrder();
                    nlo.setName(lo.getName());
                    nlo.setNumber(lo.getNumber());
                    nlo.setLessonOrderId(lo.getLessonOrderId());
                    if (lo.getQuiz() != null) {
                        Quiz quizCopy = new Quiz();
                        quizCopy.setTitle(lo.getQuiz().getTitle());
                        quizCopy.setQuizId(lo.getQuiz().getQuizId());

                        nlo.setQuiz(quizCopy);
                    }

                    if (lo.getFile() != null) {
                        File fileCopy = new File();
                        fileCopy.setName(lo.getFile().getName());
                        fileCopy.setFileId(lo.getFile().getFileId());

                        nlo.setFile(fileCopy);
                    }

                    lessonOrder.add(nlo);
                }
                newOut.setLessonOrder(lessonOrder);
                oline.add(newOut);
            }
            
            Category catout = new Category();
            catout.setCategoryId(coursepack.getCategory().getCategoryId());
            catout.setName(coursepack.getCategory().getName());

            User teacher = coursepack.getAssignedTeacher();
            User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                    teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                    null, null, null, null, null, null, null);

            Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                    coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                    coursepack.getImageLocation(), catout,
                    coursepack.getTeacherBackground(), null, null, null, null, null, oline, null, null);

            coursepackCopy.setAssignedTeacher(teacherCopy);

            return Response.status(Response.Status.OK).entity(coursepackCopy).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getCoursepackUser/{coursepackId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoursepackByIdUser(@PathParam("coursepackId") Long coursepackId, @QueryParam("userId") Long userId) {
        try {
            Coursepack coursepack = em.find(Coursepack.class, coursepackId);
            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
            }

            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorRsp("User doesn't exist!")).build();
            }

            if (!coursepack.getPublicUserList().contains(user) && !coursepack.getStudentList().contains(user)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorRsp("User is not enrolled in this coursepack!")).build();
            }

            List<Outlines> oline = new ArrayList<>();
            boolean before = true;
            Collections.sort(coursepack.getOutlineList());
            for (Outlines o : coursepack.getOutlineList()) {
                //add in lesson order (Loop inside loop)
                Outlines newOut = new Outlines();
                newOut.setName(o.getName());
                newOut.setNumber(o.getNumber());
                newOut.setOutlineId(o.getOutlineId());

                List<LessonOrder> lessonOrder = new ArrayList<>();

                Collections.sort(o.getLessonOrder());
                for (LessonOrder lo : o.getLessonOrder()) {
                    LessonOrder nlo = new LessonOrder();
                    nlo.setName(lo.getName());
                    nlo.setNumber(lo.getNumber());
                    nlo.setLessonOrderId(lo.getLessonOrderId());
                    if (lo.getQuiz() != null) {
                        Quiz quizCopy = new Quiz();
                        quizCopy.setTitle(lo.getQuiz().getTitle());
                        quizCopy.setQuizId(lo.getQuiz().getQuizId());

                        nlo.setQuiz(quizCopy);
                    }

                    if (lo.getFile() != null) {
                        File fileCopy = new File();
                        fileCopy.setName(lo.getFile().getName());
                        fileCopy.setFileId(lo.getFile().getFileId());

                        nlo.setFile(fileCopy);
                    }

                    if (lo.getPublicUserList().contains(user)) {
                        nlo.setStatus(LessonOrderStatusEnum.Completed);
                        before = true;
                    } else if (before) {
                        nlo.setStatus(LessonOrderStatusEnum.Unlocked);
                        before = false;
                    } else {
                        nlo.setStatus(LessonOrderStatusEnum.Locked);
                    }

                    lessonOrder.add(nlo);
                }
                newOut.setLessonOrder(lessonOrder);
                oline.add(newOut);
            }

            User teacher = coursepack.getAssignedTeacher();
            User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                    teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                    null, null, null, null, null, null, null);

            Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                    coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                    coursepack.getImageLocation(), null,
                    coursepack.getTeacherBackground(), null, null, null, null, null, oline, null, null);

            return Response.status(Response.Status.OK).entity(coursepackCopy).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getCoursepackByCode/{code}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoursepackByCode(@PathParam("code") String code) {
        try {
            Query query = em.createQuery("select cp from Coursepack cp where cp.code =:code");
            query.setParameter("code", code);
            Coursepack coursepack = (Coursepack) query.getSingleResult();

            if (coursepack == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Coursepack does not exist").build();
            }

            List<Outlines> oline = new ArrayList<>();
            for (Outlines o : coursepack.getOutlineList()) {
                //add in lesson order (Loop inside loop)
                Outlines newOut = new Outlines();
                newOut.setName(o.getName());
                newOut.setNumber(o.getNumber());
                newOut.setOutlineId(o.getOutlineId());

                List<LessonOrder> lessonOrder = new ArrayList<>();
                for (LessonOrder lo : lessonOrder) {
                    LessonOrder nlo = new LessonOrder();
                    nlo.setName(lo.getName());
                    nlo.setNumber(lo.getNumber());
                    if (lo.getFile() == null) {
                        Quiz quizCopy = new Quiz();
                        quizCopy.setTitle(lo.getQuiz().getTitle());
                        quizCopy.setQuizId(lo.getQuiz().getQuizId());

                        nlo.setQuiz(quizCopy);
                    } else {
                        File fileCopy = new File();
                        fileCopy.setName(lo.getFile().getName());
                        fileCopy.setFileId(lo.getFile().getFileId());

                        nlo.setFile(fileCopy);
                    }

                    lessonOrder.add(nlo);
                }
                newOut.setLessonOrder(lessonOrder);
                oline.add(newOut);
            }
            User teacher = coursepack.getAssignedTeacher();
            User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                    teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                    null, null, null, null, null, null, null);

            Coursepack coursepackCopy = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                    coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                    coursepack.getImageLocation(), null,
                    coursepack.getTeacherBackground(), null, null, null, teacherCopy, null, oline, null, null);

            return Response.status(Response.Status.OK).entity(coursepackCopy).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GET
    @Path(value = "getAllCoursepack")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCoursepack() {
        try {
            Query query = em.createQuery("select cp from Coursepack cp");
            List<Coursepack> coursepackList = query.getResultList();

            GetCoursepackRsp rsp = new GetCoursepackRsp();
            rsp.setCoursepack(new ArrayList<>());
            if (coursepackList == null && coursepackList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();
            } else {
                for (Coursepack coursepack : coursepackList) {
                    User teacher = new User();
                    teacher.setFirstName(coursepack.getAssignedTeacher().getFirstName());
                    teacher.setLastName(coursepack.getAssignedTeacher().getLastName());
                    Coursepack cpTemp = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                            coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                            coursepack.getImageLocation(), null,
                            coursepack.getTeacherBackground(), null, null, null, teacher, null, null, null, null);
                    List<Rating> ratings = coursepack.getRatingList();
                    List<Rating> rs = new ArrayList<>();
                    for (Rating r : ratings) {
                        Rating rTemp = new Rating();
                        rTemp.setRatingId(r.getRatingId());
                        rs.add(rTemp);
                    }
                    cpTemp.setRatingList(rs);
                    rsp.getCoursepack().add(cpTemp);
                }
                return Response.status(Response.Status.OK).entity(rsp).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GET
    @Path(value = "getRecommendedCoursepack")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecommendedCoursepack(@QueryParam("userId") Long userId) {
        try {
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User does not exist").build();
            }

            List<Coursepack> userCoursepackList = new ArrayList<>();
            if (user.getAccessRight() == AccessRightEnum.Public) {
                userCoursepackList = user.getPublicUserCoursepackList();
            } else if (user.getAccessRight() == AccessRightEnum.Student) {
                userCoursepackList = user.getStudentCoursepackList();
            }

            GetCoursepackRsp rsp = new GetCoursepackRsp();
            rsp.setCoursepack(new ArrayList<>());

            List<Coursepack> sameCatCoursepacks = new ArrayList<>();
            if (userCoursepackList.size() > 0) {
                sameCatCoursepacks = userCoursepackList.get(userCoursepackList.size() - 1).getCategory().getCoursepackList();
            }

            for (Coursepack coursepack : sameCatCoursepacks) {
                if (!userCoursepackList.contains(coursepack)) {
                    User teacher = new User();
                    teacher.setFirstName(coursepack.getAssignedTeacher().getFirstName());
                    teacher.setLastName(coursepack.getAssignedTeacher().getLastName());
                    Coursepack cpTemp = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                            coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                            coursepack.getImageLocation(), null,
                            coursepack.getTeacherBackground(), null, null, null, teacher, null, null, null, null);
                    List<Rating> ratings = coursepack.getRatingList();
                    List<Rating> rs = new ArrayList<>();
                    for (Rating r : ratings) {
                        Rating rTemp = new Rating();
                        rTemp.setRatingId(r.getRatingId());
                        rs.add(rTemp);
                    }
                    cpTemp.setRatingList(rs);
                    rsp.getCoursepack().add(cpTemp);
                }
            }
            return Response.status(Response.Status.OK).entity(rsp).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Path(value = "getUserCoursepack/{userId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoursepackByUserId(@PathParam("userId") Long userId) {
        try {
            User user = em.find(User.class, userId);
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("User does not exist").build();
            }

            GetCoursepackRsp rsp = new GetCoursepackRsp(new ArrayList<>());
            List<Coursepack> cp = new ArrayList<>();

            if (user.getAccessRight() == AccessRightEnum.Teacher) {
                cp = user.getTeacherCoursepackList();
            } else if (user.getAccessRight() == AccessRightEnum.Student) {
                cp = user.getStudentCoursepackList();
            } else if (user.getAccessRight() == AccessRightEnum.Public) {
                cp = user.getPublicUserCoursepackList();
            }
            if (cp == null && cp.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No coursepack found").build();
            } else {
                for (Coursepack coursepack : cp) {

                    int total = 0, progress = 0;
                    List<Outlines> oline = new ArrayList<>();
                    for (Outlines o : coursepack.getOutlineList()) {
                        Outlines newOut = new Outlines();
                        newOut.setName(o.getName());
                        newOut.setNumber(o.getNumber());
                        newOut.setOutlineId(o.getOutlineId());

                        List<LessonOrder> lessonOrder = new ArrayList<>();
                        for (LessonOrder lo : o.getLessonOrder()) {
                            LessonOrder nlo = new LessonOrder();
                            nlo.setName(lo.getName());
                            nlo.setNumber(lo.getNumber());
                            nlo.setLessonOrderId(lo.getLessonOrderId());
                            if (lo.getQuiz() != null) {
                                Quiz quizCopy = new Quiz();
                                quizCopy.setTitle(lo.getQuiz().getTitle());
                                quizCopy.setQuizId(lo.getQuiz().getQuizId());

                                nlo.setQuiz(quizCopy);
                            }

                            if (lo.getFile() != null) {
                                File fileCopy = new File();
                                fileCopy.setName(lo.getFile().getName());
                                fileCopy.setFileId(lo.getFile().getFileId());

                                nlo.setFile(fileCopy);
                            }

                            lessonOrder.add(nlo);

                            total++;
                            if (lo.getPublicUserList().contains(user)) {
                                progress++;
                            }
                        }
                        newOut.setLessonOrder(lessonOrder);
                        oline.add(newOut);
                    }

                    User teacher = new User();
                    teacher.setFirstName(coursepack.getAssignedTeacher().getFirstName());
                    teacher.setLastName(coursepack.getAssignedTeacher().getLastName());

                    List<Rating> ratings = coursepack.getRatingList();
                    List<Rating> userRatings = new ArrayList<>();
                    for (Rating r : ratings) {
                        if (r.getUser().getUserId() == user.getUserId()) {
                            Rating ratingRsp = new Rating();
                            ratingRsp.setRatingId(r.getRatingId());
                            ratingRsp.setRating(r.getRating());
                            ratingRsp.setComment(r.getComment());
                            userRatings.add(ratingRsp);
                        }
                    }

                    Coursepack cpTemp = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                            coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(),
                            coursepack.getImageLocation(), null,
                            coursepack.getTeacherBackground(), null, null, null, teacher, null, oline, null, null);
                    cpTemp.setRatingList(userRatings);

                    double progressPer = 0.0;
                    if (total > 0) {
                        progressPer = 1.0 * progress / total;
                    }
                    cpTemp.setProgress(progressPer);

                    rsp.getCoursepack().add(cpTemp);

                }
            }
            return Response.status(Response.Status.OK).entity(rsp).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getAllCategories")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        try {
            Query query = em.createQuery("select c from Category c");
            List<Category> categoryList = query.getResultList();
            GetCategoryRsp rsp = new GetCategoryRsp();

            for (Category c : categoryList) {
                rsp.getCategories().add(new Category(c.getCategoryId(), c.getName(), null));
            }
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getCategoryById")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryById(@QueryParam("categoryId") Long categoryId) {
        try {
            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("Category does not exist")).build();
            }
            List<Coursepack> coursepackList = category.getCoursepackList();
            System.out.println(coursepackList.size());
            List<Coursepack> rspCoursepacks = new ArrayList<>();
            for (Coursepack coursepack : coursepackList) {
                User teacher = new User();
                teacher.setFirstName(coursepack.getAssignedTeacher().getFirstName());
                teacher.setLastName(coursepack.getAssignedTeacher().getLastName());
                Coursepack cpTemp = new Coursepack(coursepack.getCoursepackId(), coursepack.getCode(), coursepack.getTitle(),
                        coursepack.getDescription(), coursepack.getPrice(), coursepack.getPublished(), coursepack.getRating(), coursepack.getImageLocation(), null,
                        coursepack.getTeacherBackground(), null, null, null, teacher, null, null, null, null);
                List<Rating> ratings = coursepack.getRatingList();
                System.out.println(ratings.size());
                List<Rating> rs = new ArrayList<>();
                for (Rating r : ratings) {
                    Rating rTemp = new Rating();
                    rTemp.setRatingId(r.getRatingId());
                    rs.add(rTemp);
                }
                cpTemp.setRatingList(rs);
                rspCoursepacks.add(cpTemp);
            }
            Category rsp = new Category(category.getCategoryId(), category.getName(), rspCoursepacks);
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
