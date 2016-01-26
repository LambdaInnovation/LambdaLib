package cn.lambdalib.util.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VersionUpdateUrl
{
	/**
	 * The url of the repo, like: github.com/LambdaInnovation/LambdaLib
	 */
	public String repoUrl();
}
