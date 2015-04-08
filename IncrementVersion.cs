//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This code is used to automatically increment Android project versionCode and versionName in a Manifest file.	//
//// This code also updates the project version in the pom file and then renames the apk accordingly by maven. /////
// All we have to do is place this file in the build config before we make the project in Intelli J IDEA . 
// and add a ${variable} in pom	//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////		REFERENCE:  http://stackoverflow.com/a/8156809		//////////////////////////////
//////////////// cmd line: C:\Windows\Microsoft.NET\Framework\v2.0.50727\csc IncrementVersion.cs	//////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
using System;
using System.IO;
using System.Text.RegularExpressions;
using System.Xml;

namespace AndroidAutoIncrementVersionCode
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                string pattern = @"yyyyMMdd.HHmmss";
                DateTime date = DateTime.Now;
                string thisDate = date.ToString(pattern);
                string maniFile = @"AndroidManifest.xml";
                string pomFile = @"pom.xml";
                XmlDocument xDoc = new XmlDocument();
                xDoc.Load(pomFile);
                string maniText = File.ReadAllText(maniFile);
                Regex regexVerCode = new Regex(@"(?<A>android:versionCode="")(?<VER>\d+)(?<B>"")", RegexOptions.IgnoreCase);
                Regex regexVerName = new Regex(@"(?<A>android:versionName="")(?<MAJO>\d+\.\d+)(?<B>"")", RegexOptions.IgnoreCase);
                Match matchVerCode = regexVerCode.Match(maniText);
                Match matchVerName = regexVerName.Match(maniText);
                int verCode = int.Parse(matchVerCode.Groups["VER"].Value) + 1;
                string verName = (matchVerName.Groups["MAJO"].Value);
                int numChars = verName.Length;
                string newManiText = regexVerCode.Replace(maniText, "${A}" + verCode + "${B}", 1);
                newManiText = regexVerName.Replace(newManiText, "${A}" + thisDate + "${B}", numChars);
                XmlNode root = xDoc.DocumentElement;
                if (root.HasChildNodes) {
                    foreach (XmlNode child in root)
                    {
                        if (child.Name.Equals("version"))
                        {
                            child.InnerText = thisDate;
                        }
                    }
                }
                File.WriteAllText(maniFile, newManiText);
                xDoc.Save(pomFile);
            }
            catch { }
        }
    }
}
