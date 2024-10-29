
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 牌照转移申请
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/paizhaozuanyi")
public class PaizhaozuanyiController {
    private static final Logger logger = LoggerFactory.getLogger(PaizhaozuanyiController.class);

    @Autowired
    private PaizhaozuanyiService paizhaozuanyiService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private CheliangService cheliangService;
    @Autowired
    private YonghuService yonghuService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = paizhaozuanyiService.queryPage(params);

        //字典表数据转换
        List<PaizhaozuanyiView> list =(List<PaizhaozuanyiView>)page.getList();
        for(PaizhaozuanyiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        PaizhaozuanyiEntity paizhaozuanyi = paizhaozuanyiService.selectById(id);
        if(paizhaozuanyi !=null){
            //entity转view
            PaizhaozuanyiView view = new PaizhaozuanyiView();
            BeanUtils.copyProperties( paizhaozuanyi , view );//把实体数据重构到view中

                //级联表
                CheliangEntity cheliang = cheliangService.selectById(paizhaozuanyi.getCheliangId());
                if(cheliang != null){
                    BeanUtils.copyProperties( cheliang , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setCheliangId(cheliang.getId());
                    view.setCheliangYonghuId(cheliang.getYonghuId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(paizhaozuanyi.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody PaizhaozuanyiEntity paizhaozuanyi, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,paizhaozuanyi:{}",this.getClass().getName(),paizhaozuanyi.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            paizhaozuanyi.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<PaizhaozuanyiEntity> queryWrapper = new EntityWrapper<PaizhaozuanyiEntity>()
            .eq("cheliang_id", paizhaozuanyi.getCheliangId())
            .eq("yonghu_id", paizhaozuanyi.getYonghuId())
            .eq("paizhaozuanyi_name", paizhaozuanyi.getPaizhaozuanyiName())
            .eq("paizhaozuanyi_paizhao", paizhaozuanyi.getPaizhaozuanyiPaizhao())
            .eq("paizhaozuanyi_yesno_types", paizhaozuanyi.getPaizhaozuanyiYesnoTypes())
            .eq("paizhaozuanyi_yesno_text", paizhaozuanyi.getPaizhaozuanyiYesnoText())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PaizhaozuanyiEntity paizhaozuanyiEntity = paizhaozuanyiService.selectOne(queryWrapper);
        if(paizhaozuanyiEntity==null){
            paizhaozuanyi.setPaizhaozuanyiYesnoTypes(1);
            paizhaozuanyi.setCreateTime(new Date());
            paizhaozuanyiService.insert(paizhaozuanyi);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody PaizhaozuanyiEntity paizhaozuanyi, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,paizhaozuanyi:{}",this.getClass().getName(),paizhaozuanyi.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            paizhaozuanyi.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<PaizhaozuanyiEntity> queryWrapper = new EntityWrapper<PaizhaozuanyiEntity>()
            .notIn("id",paizhaozuanyi.getId())
            .andNew()
            .eq("cheliang_id", paizhaozuanyi.getCheliangId())
            .eq("yonghu_id", paizhaozuanyi.getYonghuId())
            .eq("paizhaozuanyi_name", paizhaozuanyi.getPaizhaozuanyiName())
            .eq("paizhaozuanyi_paizhao", paizhaozuanyi.getPaizhaozuanyiPaizhao())
            .eq("paizhaozuanyi_yesno_types", paizhaozuanyi.getPaizhaozuanyiYesnoTypes())
            .eq("paizhaozuanyi_yesno_text", paizhaozuanyi.getPaizhaozuanyiYesnoText())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        PaizhaozuanyiEntity paizhaozuanyiEntity = paizhaozuanyiService.selectOne(queryWrapper);
        if(paizhaozuanyiEntity==null){
            if(paizhaozuanyi.getPaizhaozuanyiYesnoTypes() == 2){
                PaizhaozuanyiEntity paizhaozuanyiEntity1 = paizhaozuanyiService.selectById(paizhaozuanyi.getId());
                CheliangEntity cheliangEntity = new CheliangEntity();
                cheliangEntity.setId(paizhaozuanyiEntity1.getCheliangId());
                cheliangEntity.setCheliangPaizhao("无牌照");
                cheliangEntity.setPaizhaoTypes(1);
                cheliangService.updateById(cheliangEntity);
            }
            paizhaozuanyiService.updateById(paizhaozuanyi);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        paizhaozuanyiService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<PaizhaozuanyiEntity> paizhaozuanyiList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            PaizhaozuanyiEntity paizhaozuanyiEntity = new PaizhaozuanyiEntity();
//                            paizhaozuanyiEntity.setCheliangId(Integer.valueOf(data.get(0)));   //车辆 要改的
//                            paizhaozuanyiEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            paizhaozuanyiEntity.setPaizhaozuanyiName(data.get(0));                    //转移用户名称 要改的
//                            paizhaozuanyiEntity.setPaizhaozuanyiPaizhao(data.get(0));                    //牌照 要改的
//                            paizhaozuanyiEntity.setPaizhaozuanyiYesnoTypes(Integer.valueOf(data.get(0)));   //申请状态 要改的
//                            paizhaozuanyiEntity.setPaizhaozuanyiYesnoText(data.get(0));                    //申请结果 要改的
//                            paizhaozuanyiEntity.setCreateTime(date);//时间
                            paizhaozuanyiList.add(paizhaozuanyiEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        paizhaozuanyiService.insertBatch(paizhaozuanyiList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }






}
