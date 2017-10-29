#include "ui/events/blink/common.h"
//==========================================================================
// 2015/02/07: move all common functions to here (include signal processing)
//==========================================================================

void debug(const char *s,...)
{
    va_list va; va_start(va,s);
    char buffer[debug_message_max];
	vsprintf(buffer,s,va); 
    va_end(va); 
	DEBUG_MACRO(buffer);
}



