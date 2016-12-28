package com.wanpg.yauld.task

import com.wanpg.yauld.utils.FileUtils
import com.wanpg.yauld.HotFix
import com.wanpg.yauld.utils.Utils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Created by wangjinpeng on 2016/12/14.
 */
class ManifestModifyTask extends BaseTask {

    static final String AppInfoClass = "package com.wanpg.yauld;\n" +
            "\n" +
            "public class AppInfo {\n" +
            "    public static String APPLICATION_ID = \"{APPLICATION_ID}\";\n" +
            "    public static String APPLICATION_NAME = \"{APPLICATION_NAME}\";\n" +
            "    public static String VERSION = \"{VERSION}\";\n" +
            "}"


    String manifestOutPath
    public static final String YAULD_APPLICATION_ID = "com.wanpg.yauld.YauldDexApplication"

    @Override
    void onTaskExecute() {
        super.onTaskExecute()
        String applicationName = null
        String applicationId = null
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance()
            DocumentBuilder builder = builderFactory.newDocumentBuilder()
            Document document = builder.parse(new File(manifestOutPath))
            NodeList manifestList = document.getElementsByTagName("manifest")
            Element manifestElement = (Element) manifestList.item(0)
            applicationId = manifestElement.getAttribute("package")
            Utils.print("应用的applicationId --->> " + applicationId)

            Element applicationElement = (Element) manifestElement.getElementsByTagName("application").item(0)

            applicationName = applicationElement.getAttribute("android:name")
            Utils.print("应用的原applicationName ---->> " + applicationName)

            // 设置打包的
            applicationElement.setAttribute("android:name", YAULD_APPLICATION_ID)

            TransformerFactory tf = TransformerFactory.newInstance()
            Transformer transformer = tf.newTransformer()
            DOMSource source = new DOMSource(document)
            PrintWriter pw = new PrintWriter(new FileOutputStream(manifestOutPath))
            StreamResult result = new StreamResult(pw)
            transformer.transform(source, result)
        } catch (Exception e) {
            e.printStackTrace()
        }

        // 生成需要的appinfo
        if (applicationId != null && applicationName != null) {
            String tempFolder = HotFix.getTempFolder(project, flavor, buildType)
            Utils.print("临时目录:${tempFolder}")
            FileUtils.mkdirs(tempFolder)
            String appInfoProperty = "${tempFolder}${File.separator}AppInfo.java".toString()

            def replace = AppInfoClass.replace("{APPLICATION_ID}", applicationId)
                    .replace("{APPLICATION_NAME}", applicationName)
                    .replace("{VERSION}", configParams.version)
            Utils.writeToFile(appInfoProperty, replace)
        }
    }
}
