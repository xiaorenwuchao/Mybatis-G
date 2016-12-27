package xStreamTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @项目名称：wx-common
 * @类名称：Demo
 * @类描述：
 * @创建人：YangChao
 * @联系方式：18629233301@163.com
 * @创建时间：2016年12月2日 下午9:34:39
 * @version 1.0.0
 */
public class Demo {
	/*
	 * <xml> <ToUserName><![CDATA[toUser]]></ToUserName>
	 * <FromUserName><![CDATA[fromUser]]></FromUserName>
	 * <CreateTime>1348831860</CreateTime> <MsgType><![CDATA[text]]></MsgType>
	 * <Content><![CDATA[this is a test]]></Content>
	 * <MsgId>1234567890123456</MsgId> </xml>
	 */
	
	public void t1() {
//		WxClientMessage p = new WxClientMessage("toUserName", "fromUserName", "createTime", "msgType", "content",
//				"msgId", "picUrl", "mediaId", "format", "thumbMediaId", "locationX", "locationY", "scale", "label",
//				"title", "description", "url");
//		System.out.println(p.getXML());
	}
	
	@Test
	public void t2(){
		String xml = 
				"<xml>"
		  +"<ToUserName><![CDATA[toUserName]]></ToUserName>"
		  +"<FromUserName><![CDATA[fromUserName]]></FromUserName>"
		  +"<CreateTime><![CDATA[createTime]]></CreateTime>"
		  +"<MsgType><![CDATA[msgType]]></MsgType>"
		  +"<Content><![CDATA[content]]></Content>"
		  +"<MsgId><![CDATA[msgId]]></MsgId>"
		  +"<PicUrl><![CDATA[picUrl]]></PicUrl>"
		  +"<MediaId><![CDATA[mediaId]]></MediaId>"
		  +"<Format><![CDATA[format]]></Format>"
		  +"<ThumbMediaId><![CDATA[thumbMediaId]]></ThumbMediaId>"
		  +"<Location__X><![CDATA[locationX]]></Location__X>"
		  +"<Location__Y><![CDATA[locationY]]></Location__Y>"
		  +"<Scale><![CDATA[scale]]></Scale>"
		  +"<Label><![CDATA[label]]></Label>"
		  +"<Title><![CDATA[title]]></Title>"
		  +"<Description><![CDATA[description]]></Description>"
		  +"<Url><![CDATA[url]]></Url>"
		+"</xml>";
//		WxClientMessage p = (WxClientMessage) new WxClientMessage().toBean(xml);;
//		System.out.println(p.toString());
	}
}
