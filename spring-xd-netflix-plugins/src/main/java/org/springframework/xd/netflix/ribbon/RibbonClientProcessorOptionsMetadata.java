
package org.springframework.xd.netflix.ribbon;

import org.springframework.xd.http.HttpClientProcessorOptionsMetadata;
import org.springframework.xd.module.options.mixins.MappedRequestHeadersMixin;
import org.springframework.xd.module.options.mixins.MappedResponseHeadersMixin;
import org.springframework.xd.module.options.spi.Mixin;
import org.springframework.xd.module.options.spi.ModuleOption;

@Mixin({ MappedRequestHeadersMixin.Http.class, MappedResponseHeadersMixin.Http.class })
public class RibbonClientProcessorOptionsMetadata extends HttpClientProcessorOptionsMetadata {

	private String vipAddress;
	
	public String getVipAddress() {
		return vipAddress;
	}

	@ModuleOption("the vipAddress to use")
	public void setVipAddress(String vipAddress) {
		this.vipAddress = vipAddress;
	}

}
