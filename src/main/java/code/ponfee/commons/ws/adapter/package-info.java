/**
 * <pre>
 * 解决WebService输入与输出数据无法转换问题
 *  1.当形参或返回值是String、基本数据类型时，CXF可以处理
 *  2.当形参或返回值是JavaBean式的复合类型、List集合、数组时，CXF可以处理
 *  3.当形参或返回值是一些如Map、非Javabean等复合类型时，CXF无法处理
 *
 *  若还无法转换，可在接口类（interface）上加注解：@XmlSeeAlso({ String[].class, Object[].class, Object[][].class, SomeBean[].class })
 *  
 *  `@XmlJavaTypeAdapter(MarshalJsonAdapter.class)
 *  
 * </pre>
 * 
 * @author Ponfee
 */
package code.ponfee.commons.ws.adapter;
