#import "VoynichPlugin.h"
#if __has_include(<voynich/voynich-Swift.h>)
#import <voynich/voynich-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "voynich-Swift.h"
#endif

@implementation VoynichPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftVoynichPlugin registerWithRegistrar:registrar];
}
@end
