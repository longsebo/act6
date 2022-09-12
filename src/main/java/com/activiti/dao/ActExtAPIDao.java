package com.activiti.dao;

import com.activiti.model.ActExtAPIVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 接口服务定义DAO
 */
@Mapper
public interface ActExtAPIDao {
    /**
     * 功能: 传入查询参数Map，获得所有的VO对象列表，带翻页，带排序字符
     *
     * @param searchPara 搜索参数的Map
     * @param orderStr 排序字符
     * @param startIndex 开始位置(第一条是0，第二条是1...)
     * @param size 查询多少条记录(size小于等于0时,忽略翻页查询全部)
     * @return
     */
    public List<ActExtAPIVo> search(Map<String, Object> searchPara) ;
    /**
     * 查询总记录数，带查询条件
     *
     * @param searchPara 查询条件Map
     * @return 总记录数
     */
    public int getCount(Map<String, Object> searchPara);
    /**
     * 插入单条记录，用id作主键
     *
     * @param vo 用于添加的VO对象
     * @return 若添加成功，返回新生成的id
     */
    public int insert(ActExtAPIVo vo);
    /**
     * 删除单条记录
     *
     * @param serviceName 用于删除的记录的serviceName
     * @return 成功删除的记录数
     */
    public int delete(String serviceName);
    /**
     * 更新单条记录
     *
     * @param vo 用于更新的VO对象
     * @return 成功更新的记录数
     */
    public int update(ActExtAPIVo vo);
    /**
     * 查询单条记录
     *
     * @param serviceName 用于查询的记录的serviceName
     * @return 成功删除的记录数
     */
    public ActExtAPIVo get(String serviceName);
}
