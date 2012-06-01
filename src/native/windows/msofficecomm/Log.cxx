/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
#include "Log.h"

#include <stdarg.h>
#include "StringUtils.h"

FILE *Log::_stderr = NULL;

#ifdef _UNICODE
int Log::d(LPCTSTR format, LPCSTR str)
{
    LPWSTR wstr = StringUtils::MultiByteToWideChar(str);
    int ret;

    if (wstr)
    {
        ret = Log::d(format, wstr);
        ::free(wstr);
    }
    else
        ret = 0;
    return ret;
}
#endif /* #ifdef _UNICODE */

int Log::d(LPCTSTR format, ...)
{
    va_list args;

    va_start(args, format);

    int ret = ::_vftprintf(_stderr, format, args);

    ::fflush(_stderr);
    return ret;
}

LPTSTR Log::getModuleFileName()
{
    HMODULE module;
    LPTSTR ret = NULL;

    if (::GetModuleHandleEx(
            GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS
                | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
            (LPCTSTR) (Log::getModuleFileName),
            &module))
    {
        TCHAR path[MAX_PATH + 1];
        DWORD pathCapacity = sizeof(path) / sizeof(TCHAR);
        DWORD pathLength = ::GetModuleFileName(module, path, pathCapacity);

        if (pathLength && (pathLength < pathCapacity))
        {
            LPTSTR fileName = NULL;

            for (LPTSTR str = path + (pathLength - 1); str != path; str--)
            {
                TCHAR ch = *str;

                if ((ch == '\\') || (ch == '/'))
                {
                    fileName = str + 1;
                    break;
                }
                else if (ch == '.')
                    *str = '\0';
            }
            if (fileName && (*fileName != '\0'))
                ret = ::_tcsdup(fileName);
        }
    }
    return ret;
}

FILE *Log::open()
{
    LPCTSTR envVarName = _T("USERPROFILE");
    DWORD envVarValueLength1 = ::GetEnvironmentVariable(envVarName, NULL, 0);
    FILE *_stderr = NULL;

    if (envVarValueLength1)
    {
        LPTSTR moduleFileName = getModuleFileName();

        if (moduleFileName)
        {
            LPCTSTR tracing = _T("\\Tracing\\");
            size_t tracingLength = ::_tcslen(tracing);
            size_t tracingSize = sizeof(TCHAR) * tracingLength;
            size_t moduleFileNameLength = ::_tcslen(moduleFileName);
            size_t moduleFileNameSize = sizeof(TCHAR) * moduleFileNameLength;
            LPCTSTR log = _T(".log");
            size_t logLength = ::_tcslen(log);
            size_t logSize = sizeof(TCHAR) * logLength;
            LPTSTR logPath
                = (LPTSTR)
                    ::malloc(
                            sizeof(TCHAR) * envVarValueLength1
                                + tracingSize
                                + moduleFileNameSize
                                + logSize);

            if (logPath)
            {
                DWORD envVarValueLength
                    = ::GetEnvironmentVariable(
                            envVarName,
                            logPath,
                            envVarValueLength1);

                if (envVarValueLength
                        && (envVarValueLength < envVarValueLength1))
                {
                    LPTSTR str = logPath + envVarValueLength;

                    ::memcpy(str, tracing, tracingSize);
                    str += tracingLength;
                    ::memcpy(str, moduleFileName, moduleFileNameSize);
                    str += moduleFileNameLength;
                    ::memcpy(str, log, logSize);
                    str += logLength;
                    *str = '\0';

                    _stderr = ::_tfopen(logPath, _T("w"));
                }
                ::free(logPath);
            }
            ::free(moduleFileName);
        }
    }

    Log::_stderr = _stderr ? _stderr : stderr;
    return Log::_stderr;
}
