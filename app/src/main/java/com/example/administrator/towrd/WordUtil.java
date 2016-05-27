package com.example.administrator.towrd;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;

public class WordUtil {

	/**
	 * 鏍规嵁鎸囧畾鐨勫弬鏁板�笺�佹ā鏉匡紝鐢熸垚 word 鏂囨。
	 * 
	 * @param param
	 *            闇�瑕佹浛鎹㈢殑鍙橀噺
	 * @param template
	 *            妯℃澘
	 */
	public static CustomXWPFDocument generateWord(Map<String, Object> param,
			String template) {
		CustomXWPFDocument doc = null;
		try {
			OPCPackage pack = POIXMLDocument.openPackage(template);
			doc = new CustomXWPFDocument(pack);
			if (param != null && param.size() > 0) {

				// 澶勭悊娈佃惤
				List<XWPFParagraph> paragraphList = doc.getParagraphs();
				processParagraphs(paragraphList, param, doc);

				// 澶勭悊琛ㄦ牸
				Iterator<XWPFTable> it = doc.getTablesIterator();
				while (it.hasNext()) {
					XWPFTable table = it.next();
					List<XWPFTableRow> rows = table.getRows();
					for (XWPFTableRow row : rows) {
						List<XWPFTableCell> cells = row.getTableCells();
						for (XWPFTableCell cell : cells) {
							List<XWPFParagraph> paragraphListTable = cell
									.getParagraphs();
							processParagraphs(paragraphListTable, param, doc);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * 澶勭悊娈佃惤
	 * 
	 * @param paragraphList
	 */
	public static void processParagraphs(List<XWPFParagraph> paragraphList,
			Map<String, Object> param, CustomXWPFDocument doc) {
		if (paragraphList != null && paragraphList.size() > 0) {
			for (XWPFParagraph paragraph : paragraphList) {
				List<XWPFRun> runs = paragraph.getRuns();
				for (XWPFRun run : runs) {
					String text = run.getText(0);
					if (text != null) {
						boolean isSetText = false;
						for (Entry<String, Object> entry : param.entrySet()) {
							String key = entry.getKey();
							if (text.indexOf(key) != -1) {
								isSetText = true;
								Object value = entry.getValue();
								if (value instanceof String) {// 鏂囨湰鏇挎崲
									text = text.replace(key, value.toString());
									System.out.println(text);
								} else if (value instanceof Map) {// 图片替换
									text = text.replace(key, "");
									Map pic = (Map) value;
									int width = Integer.parseInt(pic.get(
											"width").toString());
									int height = Integer.parseInt(pic.get(
											"height").toString());
									int picType = getPictureType(pic
											.get("type").toString());
									byte[] byteArray =   (byte[]) pic
											.get("content");
									ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
											byteArray);
									try {
										System.out.println("图片替换");
										// int ind =
										// doc.addPicture(byteInputStream,picType);
										// doc.createPicture(ind, width ,
										// height,paragraph);
										 doc.addPictureData(byteInputStream,picType);  
									        doc.createPicture(paragraph,doc.getAllPictures().size()-1, width, height,"");  
//										CTInline inline = run.getCTR()
//												.addNewDrawing().addNewInline();
//										insertPicture(doc, byteArray, inline,
//												width, height);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
						if (isSetText) {
							run.setText(text, 0);
						}
					}
				}
			}
		}
	}

	private static void insertPicture(XWPFDocument document, String byteArray,
			CTInline inline, int width, int height)
			throws InvalidFormatException, FileNotFoundException {
		document.addPictureData(new FileInputStream(byteArray),
				XWPFDocument.PICTURE_TYPE_PNG);
		int id = document.getAllPictures().size() - 1;
		final int EMU = 9525;
		width *= EMU;
		height *= EMU;
		String blipId = document.getAllPictures().get(id)
				.getPackageRelationship().getId();
		String picXml = getPicXml(blipId, width, height);
		XmlToken xmlToken = null;
		try {
			xmlToken = XmlToken.Factory.parse(picXml);
		} catch (XmlException xe) {
			xe.printStackTrace();
		}
		inline.set(xmlToken);
		inline.setDistT(0);
		inline.setDistB(0);
		inline.setDistL(0);
		inline.setDistR(0);
		CTPositiveSize2D extent = inline.addNewExtent();
		extent.setCx(width);
		extent.setCy(height);
		CTNonVisualDrawingProps docPr = inline.addNewDocPr();
		docPr.setId(id);
		docPr.setName("IMG_" + id);
		docPr.setDescr("IMG_" + id);
	}

    private static String getPicXml(String blipId, int width, int height) {  
        String picXml =  
            "" + "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">" +  
            "   <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +  
            "      <pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">" +  
            "         <pic:nvPicPr>" + "            <pic:cNvPr id=\"" + 0 +  
            "\" name=\"Generated\"/>" + "            <pic:cNvPicPr/>" +  
            "         </pic:nvPicPr>" + "         <pic:blipFill>" +  
            "            <a:blip r:embed=\"" + blipId +  
            "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>" +  
            "            <a:stretch>" + "               <a:fillRect/>" +  
            "            </a:stretch>" + "         </pic:blipFill>" +  
            "         <pic:spPr>" + "            <a:xfrm>" +  
            "               <a:off x=\"0\" y=\"0\"/>" +  
            "               <a:ext cx=\"" + width + "\" cy=\"" + height +  
            "\"/>" + "            </a:xfrm>" +  
            "            <a:prstGeom prst=\"rect\">" +  
            "               <a:avLst/>" + "            </a:prstGeom>" +  
            "         </pic:spPr>" + "      </pic:pic>" +  
            "   </a:graphicData>" + "</a:graphic>";  
        return picXml;  
    } 

	/**
	 * 鏍规嵁鍥剧墖绫诲瀷锛屽彇寰楀搴旂殑鍥剧墖绫诲瀷浠ｇ爜
	 * 
	 * @param picType
	 * @return int
	 */
	private static int getPictureType(String picType) {
		int res = CustomXWPFDocument.PICTURE_TYPE_PICT;
		if (picType != null) {
			if (picType.equalsIgnoreCase("png")) {
				res = CustomXWPFDocument.PICTURE_TYPE_PNG;
			} else if (picType.equalsIgnoreCase("dib")) {
				res = CustomXWPFDocument.PICTURE_TYPE_DIB;
			} else if (picType.equalsIgnoreCase("emf")) {
				res = CustomXWPFDocument.PICTURE_TYPE_EMF;
			} else if (picType.equalsIgnoreCase("jpg")
					|| picType.equalsIgnoreCase("jpeg")) {
				res = CustomXWPFDocument.PICTURE_TYPE_JPEG;
			} else if (picType.equalsIgnoreCase("wmf")) {
				res = CustomXWPFDocument.PICTURE_TYPE_WMF;
			}
		}
		return res;
	}

	/**
	 * 灏嗚緭鍏ユ祦涓殑鏁版嵁鍐欏叆瀛楄妭鏁扮粍
	 * 
	 * @param in
	 * @return
	 */
	public static byte[] inputStream2ByteArray(InputStream in, boolean isClose) {
		byte[] byteArray = null;
		try {
			int total = in.available();
			byteArray = new byte[total];
			in.read(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isClose) {
				try {
					in.close();
				} catch (Exception e2) {
					System.out.println("鍏抽棴娴佸け璐�");
				}
			}
		}
		return byteArray;
	}
}