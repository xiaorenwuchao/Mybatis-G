package mybatisGenerator;

import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * @项目名称：project-common
 * @类名称：PaginationPlugin
 * @类描述：自定义代码生成器
 * @创建人：YangChao
 * @作者单位：北京宝库在线网络技术有限公司
 * @联系方式：YangChao@baoku.com
 * @创建时间：2016年9月5日 下午3:14:38
 * @version 1.0.0
 */
public class PaginationPlugin extends PluginAdapter {
	/**
	 * 生成dao
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("BaseDao<" + introspectedTable.getBaseRecordType()
				+ ">");
		FullyQualifiedJavaType imp = new FullyQualifiedJavaType("com.base.BaseDao");
		interfaze.addSuperInterface(fqjt);// 添加 extends BaseDao<User>
		interfaze.addImportedType(imp);// 添加import common.BaseDao;
		interfaze.getMethods().clear();
		return true;
	}

	/**
	 * 生成实体中每个属性
	 */
	@Override
	public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
			IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
		if (introspectedColumn.getActualColumnName().equals("id"))
			return false;
		return true;
	}

	/**
	 * 生成实体
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		addSerialVersionUID(topLevelClass, introspectedTable);
		generateToString(introspectedTable, topLevelClass);
		FullyQualifiedJavaType imp = new FullyQualifiedJavaType("com.thoughtworks.xstream.annotations.XStreamAlias");
		topLevelClass.addImportedType(imp);
		for(Field f: topLevelClass.getFields()){
			String anno= "@XStreamAlias(\""+f.getName()+"\")";
			f.addAnnotation(anno);
		}
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 生成mapping
	 */
	@Override
	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
		return super.sqlMapGenerated(sqlMap, introspectedTable);
	}

	/**
	 * 生成mapping 添加自定义sql
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();// 数据库表名
		List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
		XmlElement parentElement = document.getRootElement();

		// 添加sql——where
		XmlElement sql = new XmlElement("sql");
		sql.addAttribute(new Attribute("id", "sql_where"));
		XmlElement where = new XmlElement("where");
		StringBuilder sb = new StringBuilder();
		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
			XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
			sb.setLength(0);
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" != null"); //$NON-NLS-1$
			isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
			where.addElement(isNotNullElement);

			sb.setLength(0);
			sb.append(" and ");
			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			sb.append(" = "); //$NON-NLS-1$
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
			isNotNullElement.addElement(new TextElement(sb.toString()));
		}
		sql.addElement(where);
		parentElement.addElement(sql);

		// 添加getList
		XmlElement select = new XmlElement("select");
		select.addAttribute(new Attribute("id", "getList"));
		select.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		select.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		select.addElement(new TextElement(" select * from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

		XmlElement include = new XmlElement("include");
		include.addAttribute(new Attribute("refid", "sql_where"));

		select.addElement(include);
		parentElement.addElement(select);

		// 添加getCountSelective
		XmlElement select2 = new XmlElement("select");
		select2.addAttribute(new Attribute("id", "getCountSelective"));
		select2.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
		select2.addElement(new TextElement(" select count(1) from "
				+ introspectedTable.getFullyQualifiedTableNameAtRuntime()));
		select2.addElement(include);
		parentElement.addElement(select2);

		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		// LIMIT5,10; // 检索记录行 6-15
		//		XmlElement isNotNullElement = new XmlElement("if");//$NON-NLS-1$
		//		isNotNullElement.addAttribute(new Attribute("test", "limitStart != null and limitStart >=0"));//$NON-NLS-1$ //$NON-NLS-2$
		// isNotNullElement.addElement(new
		// TextElement("limit ${limitStart} , ${limitEnd}"));
		// element.addElement(isNotNullElement);
		// LIMIT 5;//检索前 5个记录行
		return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	/**
	 * mapping中添加方法
	 */
	// @Override
	public boolean sqlMapDocumentGenerated2(Document document, IntrospectedTable introspectedTable) {
		String tableName = introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();// 数据库表名
		List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
		// 添加sql
		XmlElement sql = new XmlElement("select");

		XmlElement parentElement = document.getRootElement();
		XmlElement deleteLogicByIdsElement = new XmlElement("update");
		deleteLogicByIdsElement.addAttribute(new Attribute("id", "deleteLogicByIds"));
		deleteLogicByIdsElement
				.addElement(new TextElement(
						"update "
								+ tableName
								+ " set deleteFlag = #{deleteFlag,jdbcType=INTEGER} where id in "
								+ " <foreach item=\"item\" index=\"index\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> "));

		parentElement.addElement(deleteLogicByIdsElement);
		XmlElement queryPage = new XmlElement("select");
		queryPage.addAttribute(new Attribute("id", "queryPage"));
		queryPage.addAttribute(new Attribute("resultMap", "BaseResultMap"));
		queryPage.addElement(new TextElement("select "));

		XmlElement include = new XmlElement("include");
		include.addAttribute(new Attribute("refid", "Base_Column_List"));

		queryPage.addElement(include);
		queryPage.addElement(new TextElement(" from " + tableName + " ${sql}"));
		parentElement.addElement(queryPage);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	private void addSerialVersionUID(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(new FullyQualifiedJavaType("long"));
		field.setStatic(true);
		field.setFinal(true);
		field.setName("serialVersionUID");
		field.setInitializationString("1L");
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
	}

	/*
	 * Dao中添加方法
	 */
	private Method generateDeleteLogicByIds(Method method, IntrospectedTable introspectedTable) {
		Method m = new Method("deleteLogicByIds");
		m.setVisibility(method.getVisibility());
		m.setReturnType(FullyQualifiedJavaType.getIntInstance());
		m.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "deleteFlag", "@Param(\"deleteFlag\")"));
		m.addParameter(new Parameter(new FullyQualifiedJavaType("Integer[]"), "ids", "@Param(\"ids\")"));
		context.getCommentGenerator().addGeneralMethodComment(m, introspectedTable);
		return m;
	}

	/*
	 * 实体中添加属性
	 */
	private void addLimit(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String name) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		Field field = new Field();
		field.setVisibility(JavaVisibility.PROTECTED);
		field.setType(FullyQualifiedJavaType.getIntInstance());
		field.setName(name);
		field.setInitializationString("-1");
		commentGenerator.addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);
		char c = name.charAt(0);
		String camel = Character.toUpperCase(c) + name.substring(1);
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("set" + camel);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), name));
		method.addBodyLine("this." + name + "=" + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.setName("get" + camel);
		method.addBodyLine("return " + name + ";");
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);
	}

	public boolean validate(List<String> warnings) {
		return true;
	}

	/**
	 * 添加额外的自定义生成的java文件
	 */
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
		// TODO Auto-generated method stub
		return super.contextGenerateAdditionalJavaFiles(introspectedTable);
	}

	/*
	 * 生成自定义的toString()
	 */
	private void generateToString(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
		// 首先创建一个Method对象，注意，这个Method是org.mybatis.generator.api.dom.java.Method，
		// 这个Method是MBG中对对象DOM的一个抽象；因为我们要添加方法，所以先创建一个；
		Method method = new Method();

		// 设置这个方法的可见性为public，代码已经在一步一步构建这个方法了
		method.setVisibility(JavaVisibility.PUBLIC);

		// 设置方法的返回类型，这里注意一下的就是，returnType是一个FullyQualifiedJavaType；
		// 这个FullyQualifiedJavaType是MGB中对Java中的类型的一个DOM封装，这个类在整个MBG中大量使用；
		// FullyQualifiedJavaType提供了几个静态的方法，比如getStringInstance，就直接返回了一个对String类型的封装；
		method.setReturnType(FullyQualifiedJavaType.getStringInstance());

		// 设置方法的名称，至此，方法签名已经装配完成；
		method.setName("toString"); //$NON-NLS-1$

		// 判断当前MBG运行的环境是否支持Java5（这里就可以看出来IntrospectedTable类的作用了，主要是查询生成环境的作用）
		if (introspectedTable.isJava5Targeted()) {
			// 如果支持Java5，就在方法上面生成一个@Override标签；
			method.addAnnotation("@Override"); //$NON-NLS-1$
		}
		// 访问上下文对象（这个context对象是在PluginAdapter初始化完成后，通过setContext方法设置进去的，
		// 通过getCommentGenerator方法得到注释生成器，并调用addGeneralMethodComment为当前生成的方法添加注释；
		// 因为我们没有提供自己的注释生成器，所以默认的注释生成器只是说明方法是通过MBG生成的，对应的是哪个表而已；
		// 这句代码其实非常有意义，通过这句代码，我们基本就可能了解到MBG中对于方法注释的生成方式了；
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

		// OK，调用addBodyLine开始为方法添加代码了
		// 可以看到，确实，只是简单的把要生成的代码通过String拼装到了method的body中而已；
		// 想到了什么？确实，我想到了Servelt的输出方法。MBG默认的方法体具体的实现，就是像Servlet那样通过String输出的；
		// 所以，这才会为我们后面准备用Velocity来重写MBG提供了依据，我们只是给MBG添加一个MVC的概念；
		method.addBodyLine("StringBuilder sb = new StringBuilder();"); //$NON-NLS-1$
		method.addBodyLine("sb.append(getClass().getSimpleName());"); //$NON-NLS-1$
		method.addBodyLine("sb.append(\" [\");"); //$NON-NLS-1$
		method.addBodyLine("sb.append(\"id = \").append(id);"); //$NON-NLS-1$

		// 接下来要准备拼装类的字段了；
		StringBuilder sb = new StringBuilder();

		// 通过topLevelClass得到当前类的所有字段，注意，这里的Field是org.mybatis.generator.api.dom.java.Field
		// 这个Field是MBG对字段的一个DOM封装
		for (Field field : topLevelClass.getFields()) {
			// 得到字段的名称；
			String property = field.getName();
			if ("serialVersionUID".equals(property))
				continue;
			// 重置StringBuilder；
			sb.setLength(0);

			// 添加字段的输出代码；
			sb.append("sb.append(\"").append(", ").append(property) //$NON-NLS-1$ //$NON-NLS-2$
					.append("=\")").append(".append(").append(property) //$NON-NLS-1$ //$NON-NLS-2$
					.append(");"); //$NON-NLS-1$

			// 把这个字段的toString输出到代码中；所以才看到我们最后生成的代码结果中，每一个字段在toString方法中各占一行；
			method.addBodyLine(sb.toString());
		}

		method.addBodyLine("sb.append(\"]\");"); //$NON-NLS-1$
		method.addBodyLine("return sb.toString();"); //$NON-NLS-1$

		// 把拼装好的方法DOM添加到topLevelClass中，完成方法添加；
		topLevelClass.addMethod(method);
	}

}
