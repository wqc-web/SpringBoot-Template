package com.zhongzhou.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhongzhou.api.entity.Contingent;
import com.zhongzhou.api.service.impl.ContingentServiceImpl;
import com.zhongzhou.common.base.BaseController;
import com.zhongzhou.common.base.Pager;
import com.zhongzhou.common.bean.ReturnEntity;
import com.zhongzhou.common.bean.ReturnEntityError;
import com.zhongzhou.common.bean.ReturnEntitySuccess;
import com.zhongzhou.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍
 *
 *
 * @since 2021-03-19
 */
@RestController
@RequestMapping("/api/contingent")
@Slf4j
public class ContingentController extends BaseController {

    private static final long serialVersionUID = 8828640983582531089L;

    @Resource
    private ContingentServiceImpl contingentService;

    /**
     * 分页查询列表
     *
     * @param current    当前页码
     * @param size       每页数量
     * @param contingent Contingent
     * @return ReturnEntity
     */
    @GetMapping("/page")
    public ReturnEntity selectPageList(Contingent contingent, Integer current, Integer size,
                                       HttpServletRequest request, HttpServletResponse response) {
        try {
            Pager<Contingent> pager = new Pager<>(current, size);
            QueryWrapper<Contingent> wrapper = new QueryWrapper<>();
            wrapper.like(StringUtils.isNotBlank(contingent.getName()), "name", contingent.getName());
            wrapper.orderByDesc("id");
            List<Contingent> records = contingentService.page(pager, wrapper).getRecords();
            int count = contingentService.count(wrapper);
            return new ReturnEntitySuccess(Constants.MSG_FIND_SUCCESS, count, records);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[" + Constants.MSG_FIND_FAILED + "]:" + e.getMessage());
            return new ReturnEntityError(e.getMessage());
        }
    }

    /**
     * 查询所有列表
     *
     * @param contingent Contingent
     * @return ReturnEntity
     */
    @GetMapping("/list")
    public ReturnEntity selectList(Contingent contingent,
                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            QueryWrapper<Contingent> wrapper = new QueryWrapper<>();
            wrapper.like(StringUtils.isNotBlank(contingent.getName()), "name", contingent.getName());
            wrapper.orderByDesc("id");
            List<Contingent> list = contingentService.list(wrapper);
            return new ReturnEntitySuccess(Constants.MSG_FIND_SUCCESS, list.size(), list);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[" + Constants.MSG_FIND_FAILED + "]:" + e.getMessage());
            return new ReturnEntityError(e.getMessage());
        }
    }

    /**
     * 查询详情
     *
     * @param id 主键
     * @return ReturnEntity
     */
    @GetMapping("/detail/{id}")
    public ReturnEntity selectById(@PathVariable("id") Long id) {
        try {
            Contingent contingent = contingentService.getById(id);
            if (null != contingent) {
                return new ReturnEntitySuccess(Constants.MSG_FIND_SUCCESS, contingent);
            } else {
                return new ReturnEntitySuccess(Constants.MSG_FIND_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[id:{} " + Constants.MSG_FIND_FAILED + "]:{}", id, e.getMessage());
            return new ReturnEntityError(e.getMessage());
        }
    }

    /**
     * 新增
     *
     * @param contingent Contingent
     * @param result     BindingResult
     * @return ReturnEntity
     */
    @PostMapping("/add")
    public ReturnEntity save(@Validated @RequestBody Contingent contingent, BindingResult result,
                             HttpServletRequest request, HttpServletResponse response) {
        if (result.hasErrors()) {
            FieldError fieldError = result.getFieldErrors().get(0);
            String errorMsg = fieldError.getDefaultMessage();
            if (Constants.MSG_ERROR_CANNOT_NULL.equals(errorMsg)) {
                errorMsg = fieldError.getField() + fieldError.getDefaultMessage();
            }
            return new ReturnEntityError(errorMsg, null, contingent);
        } else {
            try {
                contingent.setCreateTime(LocalDateTime.now());
                contingent.setCreateUserId(tokenController.getUserId(request, response));
                if (contingentService.save(contingent)) {
                    contingentService.initList();
                    return new ReturnEntitySuccess(Constants.MSG_INSERT_SUCCESS, contingent);
                } else {
                    return new ReturnEntityError(Constants.MSG_INSERT_FAILED, contingent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("[" + Constants.MSG_INSERT_FAILED + "]:{}", e.getMessage());
                return new ReturnEntityError(e.getMessage());
            }
        }
    }

    /**
     * 修改
     *
     * @param id         主键
     * @param contingent Contingent
     * @param result     BindingResult
     * @return ReturnEntity
     */
    @PutMapping("/edit/{id}")
    public ReturnEntity updateById(@PathVariable("id") Long id, @Validated @RequestBody Contingent contingent, BindingResult result,
                                   HttpServletRequest request, HttpServletResponse response) {
        if (result.hasErrors()) {
            return new ReturnEntityError(result.getFieldErrors().get(0).getDefaultMessage(), contingent);
        } else {
            try {
                if (null == contingentService.getById(id)) {
                    return new ReturnEntityError(Constants.MSG_FIND_NOT_FOUND, contingent);
                } else {
                    contingent.setId(id);
                    if (contingentService.updateById(contingent)) {
                        contingentService.initList();
                        return new ReturnEntitySuccess(Constants.MSG_UPDATE_SUCCESS, contingent);
                    } else {
                        return new ReturnEntityError(Constants.MSG_UPDATE_FAILED, contingent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("[id:{} " + Constants.MSG_UPDATE_FAILED + "]:{}", id, e.getMessage());
                return new ReturnEntityError(e.getMessage());
            }
        }
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return ReturnEntity
     */
    @DeleteMapping("/delete/{id}")
    public ReturnEntity deleteById(@PathVariable("id") Long id) {
        try {
            if (null == contingentService.getById(id)) {
                return new ReturnEntityError(Constants.MSG_FIND_NOT_FOUND, id);
            } else {
                if (contingentService.removeById(id)) {
                    contingentService.initList();
                    return new ReturnEntitySuccess(Constants.MSG_DELETE_SUCCESS, id);
                } else {
                    return new ReturnEntityError(Constants.MSG_DELETE_FAILED, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[id:{} " + Constants.MSG_DELETE_FAILED + "]:{}", id, e.getMessage());
            return new ReturnEntityError(e.getMessage());
        }
    }

}
