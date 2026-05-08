package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.model.dto.request.admin.*;
import com.xunyu.codenexus.backend.model.dto.response.admin.*;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ExampleVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemTestcase;

import java.util.List;

/**
 * 管理后台核心业务接口
 */
public interface AdminService {

    // ========== 用户管理 ==========

    Page<AdminUserVO> getUserPage(AdminUserQueryRequest request);

    AdminUserDetailVO getUserDetail(Long userId);

    void updateUserRole(Long userId, String role);

    void banUser(Long userId);

    void unbanUser(Long userId);

    // ========== 题库管理 ==========

    Page<AdminProblemVO> getProblemPage(AdminProblemQueryRequest request);

    AdminProblemDetailVO getProblemDetail(Long problemId);

    Long createProblem(ProblemUpsertRequest request);

    void updateProblem(Long problemId, ProblemUpsertRequest request);

    void deleteProblem(Long problemId);

    void publishProblem(Long problemId);

    void offlineProblem(Long problemId);

    List<ProblemTestcase> getTestcases(Long problemId);

    Long addTestcase(Long problemId, ProblemTestcase testcase);

    void updateTestcase(Long testcaseId, ProblemTestcase testcase);

    void deleteTestcase(Long testcaseId);

    // ========== 题解管理 ==========

    List<SolutionVO> getProblemSolutions(Long problemId);

    void publishOfficialSolution(Long problemId, SolutionAddRequest request);

    void deleteSolution(Long solutionId);

    // ========== 对战记录 ==========

    Page<AdminArenaRoomVO> getArenaRoomPage(AdminArenaQueryRequest request);

    AdminArenaRoomVO getArenaRoomDetail(Long roomId);

    Page<AdminArenaRoomVO> getAbnormalArenaRooms(AdminArenaQueryRequest request);

    // ========== 数据统计 ==========

    AdminStatsOverviewVO getStatsOverview();

    List<DailyTrendVO> getStatsTrends(int days);

    ActivityAnalysisVO getStatsActivity();
}
