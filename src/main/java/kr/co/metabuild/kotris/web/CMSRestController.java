package kr.co.metabuild.kotris.web;

import kr.co.metabuild.kotris.config.AdaptorPropPrefix;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@ConditionalOnProperty(name = AdaptorPropPrefix.CMS_ADAPTOR_PREFIX + ".enabled", matchIfMissing = false)
public class CMSRestController {
}
