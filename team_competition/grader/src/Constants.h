#ifndef __SMOC_GRADER_CONSTANTS_H__
#define __SMOC_GRADER_CONSTANTS_H__

#import <string>

class Constants {
public:
    static std::string CONFIG_FILE;
    
    enum {
        SERVER_PORT = 7263,
    };
};

#endif  // __SMOC_GRADER_CONSTANTS_H__ 
