/**
 * IK 中文分词  版本 5.0.1
 * IK Analyzer release 5.0.1
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 * 
 * 
 */
package org.wltea.analyzer.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * 使用IKAnalyzer进行分词的演示
 * 2012-10-22
 *
 */
public class IKAnalzyerDemo {
	
	//构建IK分词器，使用smart分词模式
	static Analyzer analyzer = new IKAnalyzer(true);
	//获取Lucene的TokenStream对象
    static TokenStream ts = null;
    
    static FileWriter fw = null;
    final static String sDestFile = "result.txt";
    static File destFile;
    
	public static void main(String[] args){
		//StringBuilder strContent = readFileByLines("360buytitle.txt");
			
		try {
			destFile = new File(sDestFile);
			if (!destFile.exists()) {
			destFile.createNewFile();
			}  
			fw = new FileWriter(destFile);
			ts = analyzer.tokenStream("myfield", new StringReader("gdg"));
			//ts = analyzer.tokenStream("myfield", new StringReader(strContent.toString()));
			
			/*Long time1 = System.currentTimeMillis();
			HandleFileByLines("test.txt");
			Long time2 = System.currentTimeMillis();
			System.out.println("分词完成，IKAnalyzer用时： "+(time2 - time1)+"ms");*/
			
			//获取词元位置属性
		    OffsetAttribute  offset = ts.addAttribute(OffsetAttribute.class); 
		    //获取词元文本属性
		    CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
		    //获取词元文本属性
		    TypeAttribute type = ts.addAttribute(TypeAttribute.class);
		    
		    
		    //重置TokenStream（重置StringReader）
			ts.reset();
			//迭代获取分词结果
			Long time1 = System.currentTimeMillis();
			while (ts.incrementToken()) {
				//fw.write(term.toString()+" ");
			  System.out.println(offset.startOffset() + " - " + offset.endOffset() + " : " + term.toString() + " | " + type.type());
			}
			Long time2 = System.currentTimeMillis();
			System.out.println("IKAnalyzer用时： "+(time2 - time1)+"ms");
			//关闭TokenStream（关闭StringReader）
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//释放TokenStream的所有资源
			if(ts != null){
		      try {
				ts.close();
		      } catch (IOException e) {
				e.printStackTrace();
		      }
			}
			try {
				fw.close();
				fw = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	    }	
	}
	
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static StringBuilder readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
            	result.append(tempString);      
            }
            reader.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.printStackTrace();
                }
            }
        }
		return result;
    }
    

    public static void  HandleFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            //System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
           // int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
            	//result.append(tempString);
            	myIKSegment(tempString);
            	
                //System.out.println("line " + line + ": " + tempString);
               // line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                	e1.printStackTrace();
                }
            }
        }
    }
    
    private static void myIKSegment(String content)
    {
    	try {			
			ts = analyzer.tokenStream("myfield", new StringReader(content));
		    //获取词元文本属性
		    CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);    
		    
		    //重置TokenStream（重置StringReader）
			ts.reset();
			//迭代获取分词结果
			while (ts.incrementToken()) {
				fw.write(term.toString()+" ");
			}
			fw.write("\r\n");
			//关闭TokenStream（关闭StringReader）
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
