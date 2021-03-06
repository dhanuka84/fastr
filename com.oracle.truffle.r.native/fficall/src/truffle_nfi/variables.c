/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
#include <string.h>
#include <Rinterface.h>
#include <trufflenfi.h>
#include <rffiutils.h>
#include "../common/rffi_variablesindex.h"


static const char *R_Home_static;
static const char *R_TempDir_static;
static SEXP R_EmptyEnv_static;
static SEXP R_Srcref_static;
static SEXP R_NilValue_static;
static SEXP R_NilValue_static2;
static SEXP R_UnboundValue_static;
static SEXP R_MissingArg_static;
static SEXP R_BaseSymbol_static;
static SEXP R_Bracket2Symbol_static;   /* "[[" */
static SEXP R_BracketSymbol_static;    /* "[" */
static SEXP R_BraceSymbol_static;      /* "{" */
static SEXP R_DoubleColonSymbol_static;/* "::" */
static SEXP R_ClassSymbol_static;     /* "class" */
static SEXP R_DeviceSymbol_static;     /* ".Device" */
static SEXP R_DevicesSymbol_static;     /* ".Devices" */
static SEXP R_DimNamesSymbol_static;   /* "dimnames" */
static SEXP R_DimSymbol_static;     /* "dim" */
static SEXP R_DollarSymbol_static;     /* "$" */
static SEXP R_DotsSymbol_static;     /* "..." */
static SEXP R_DropSymbol_static;     /* "drop" */
static SEXP R_LastvalueSymbol_static;  /* ".Last.value" */
static SEXP R_LevelsSymbol_static;     /* "levels" */
static SEXP R_ModeSymbol_static;     /* "mode" */
static SEXP R_NameSymbol_static;     /* "name" */
static SEXP R_NamesSymbol_static;     /* "names" */
static SEXP R_NaRmSymbol_static;     /* "na.rm" */
static SEXP R_PackageSymbol_static;    /* "package" */
static SEXP R_QuoteSymbol_static;     /* "quote" */
static SEXP R_RowNamesSymbol_static;   /* "row.names" */
static SEXP R_SeedsSymbol_static;     /* ".Random.seed" */
static SEXP R_SourceSymbol_static;     /* "source" */
static SEXP R_TspSymbol_static;     /* "tsp" */
static SEXP R_dot_defined_static;      /* ".defined" */
static SEXP R_dot_Method_static;       /* ".Method" */
static SEXP R_dot_target_static;       /* ".target" */
static SEXP R_NaString_static;	    /* NA_STRING as a CHARSXP */
static SEXP R_BlankString_static;	    /* "" as a CHARSXP */
static SEXP R_BlankScalarString_static;	    /* "" as a STRSXP */
static SEXP R_BaseSymbol_static;	    /* "base" as a SYMSXP */
static SEXP	R_NamespaceEnvSymbol_static;   // ".__NAMESPACE__."

// Symbols not part of public API but used in FastR tools implementation
static SEXP R_SrcrefSymbol_static;
static SEXP R_SrcfileSymbol_static;
static SEXP R_RestartToken_static;

// Arith.h
double R_NaN;		/* IEEE NaN */
double R_PosInf;	/* IEEE Inf */
double R_NegInf;	/* IEEE -Inf */
double R_NaReal;	/* NA_REAL: IEEE */
int R_NaInt;	/* NA_INTEGER:= INT_MIN currently */

// various ignored flags and variables nevertheless needed to resolve symbols
Rboolean R_Visible;
Rboolean R_interrupts_suspended;
int R_interrupts_pending;
Rboolean mbcslocale;
Rboolean useaqua;
char* OutDec = ".";
Rboolean utf8locale = FALSE;
Rboolean mbcslocale = FALSE;
Rboolean latin1locale = FALSE;
int R_dec_min_exponent = -308;
int max_contour_segments = 25000;

// from sys-std.c
#include <R_ext/eventloop.h>

static InputHandler BasicInputHandler = {2, -1, NULL};
InputHandler *R_InputHandlers = &BasicInputHandler;

char *FASTR_R_Home() {
	return (char *) R_Home_static;
}

char *FASTR_R_TempDir() {
	return (char *) R_TempDir_static;
}

SEXP FASTR_R_EmptyEnv() {
    return R_EmptyEnv_static;
}

SEXP FASTR_R_Srcref() {
    return R_Srcref_static;
}

SEXP FASTR_R_NilValue() {
    return R_NilValue_static;
}

SEXP FASTR_R_UnboundValue() {
    return R_UnboundValue_static;
}

SEXP FASTR_R_MissingArg() {
    return R_MissingArg_static;
}

SEXP FASTR_R_BaseSymbol() {
    return R_BaseSymbol_static;
}


SEXP FASTR_R_BraceSymbol() {
    return R_BraceSymbol_static;
}

SEXP FASTR_R_DoubleColonSymbol() {
    return R_DoubleColonSymbol_static;
}

SEXP FASTR_R_Bracket2Symbol() {
    return R_Bracket2Symbol_static;
}

SEXP FASTR_R_BracketSymbol() {
    return R_BracketSymbol_static;
}

SEXP FASTR_R_ClassSymbol() {
    return R_ClassSymbol_static;
}

SEXP FASTR_R_DimNamesSymbol() {
    return R_DimNamesSymbol_static;
}

SEXP FASTR_R_DimSymbol() {
    return R_DimSymbol_static;
}


SEXP FASTR_R_DollarSymbol() {
    return R_DollarSymbol_static;
}

SEXP FASTR_R_DotsSymbol() {
    return R_DotsSymbol_static;
}


SEXP FASTR_R_DropSymbol() {
    return R_DropSymbol_static;
}

SEXP FASTR_R_LastvalueSymbol() {
    return R_LastvalueSymbol_static;
}


SEXP FASTR_R_LevelsSymbol() {
    return R_LevelsSymbol_static;
}

SEXP FASTR_R_ModeSymbol() {
    return R_ModeSymbol_static;
}

SEXP FASTR_R_NaRmSymbol() {
    return R_NaRmSymbol_static;
}


SEXP FASTR_R_NameSymbol() {
    return R_NameSymbol_static;
}

SEXP FASTR_R_NamesSymbol() {
    return R_NamesSymbol_static;
}


SEXP FASTR_R_NamespaceEnvSymbol() {
    return R_NamespaceEnvSymbol_static;
}

SEXP FASTR_R_PackageSymbol() {
    return R_PackageSymbol_static;
}

SEXP FASTR_R_QuoteSymbol() {
    return R_QuoteSymbol_static;
}

SEXP FASTR_R_RowNamesSymbol() {
    return R_RowNamesSymbol_static;
}

SEXP FASTR_R_SeedsSymbol() {
    return R_SeedsSymbol_static;
}

SEXP FASTR_R_SourceSymbol() {
    return R_SourceSymbol_static;
}

SEXP FASTR_R_TspSymbol() {
    return R_TspSymbol_static;
}

SEXP FASTR_R_dot_defined() {
    return R_dot_defined_static;
}

SEXP FASTR_R_dot_Method() {
    return R_dot_Method_static;
}

SEXP FASTR_R_dot_target() {
    return R_dot_target_static;
}

SEXP FASTR_R_NaString() {
    return R_NaString_static;
}


SEXP FASTR_R_BlankString() {
    return R_BlankString_static;
}

SEXP FASTR_R_BlankScalarString() {
    return R_BlankScalarString_static;
}

SEXP FASTR_R_DevicesSymbol() {
    return R_DevicesSymbol_static;
}

SEXP FASTR_R_DeviceSymbol() {
    return R_DeviceSymbol_static;
}

SEXP FASTR_R_SrcrefSymbol() {
    return R_SrcrefSymbol_static;
}

SEXP FASTR_R_SrcfileSymbol() {
    return R_SrcfileSymbol_static;
}

void Call_initvar_double(int index, double value) {
	switch (index) {
    case R_NaN_x: R_NaN = value; break;
	}
}

void Call_initvar_int(int index, int value) {
	switch (index) {
    case R_NaInt_x: R_NaInt = value; break;
    case R_PosInf_x: R_PosInf = value; break;
    case R_NegInf_x: R_NegInf = value; break;
    case R_NaReal_x: R_NaReal = value; break;
	}
}

char *copystring(char *value) {
	char *result = malloc(strlen(value) + 1);
	strcpy(result, value);
	return result;
}

// value must be copied
void Call_initvar_string(int index, char *value) {
	switch (index) {
    case R_Home_x: R_Home_static = copystring(value); break;
    case R_TempDir_x: R_TempDir_static = copystring(value); break;
	}
}

void Call_initvar_obj(TruffleEnv* env, int index, void* value) {
	init_utils(env);
	switch (index) {
    case R_NilValue_x: R_NilValue_static = createGlobalRef(value, 1); break;
    case R_UnboundValue_x: R_UnboundValue_static = createGlobalRef(value, 1); break;
    case R_MissingArg_x: R_MissingArg_static = createGlobalRef(value, 1); break;
    case R_Srcref_x: R_Srcref_static = createGlobalRef(value, 1); break;
    case R_EmptyEnv_x: R_EmptyEnv_static = createGlobalRef(value, 1); break;
    case R_Bracket2Symbol_x: R_Bracket2Symbol_static = createGlobalRef(value, 1); break;
    case R_BracketSymbol_x: R_BracketSymbol_static = createGlobalRef(value, 1); break;
    case R_BraceSymbol_x: R_BraceSymbol_static = createGlobalRef(value, 1); break;
    case R_DoubleColonSymbol_x: R_DoubleColonSymbol_static = createGlobalRef(value, 1); break;
    case R_ClassSymbol_x: R_ClassSymbol_static = createGlobalRef(value, 1); break;
    case R_DeviceSymbol_x: R_DeviceSymbol_static = createGlobalRef(value, 1); break;
    case R_DevicesSymbol_x: R_DevicesSymbol_static = createGlobalRef(value, 1); break;
    case R_DimNamesSymbol_x: R_DimNamesSymbol_static = createGlobalRef(value, 1); break;
    case R_DimSymbol_x: R_DimSymbol_static = createGlobalRef(value, 1); break;
    case R_DollarSymbol_x: R_DollarSymbol_static = createGlobalRef(value, 1); break;
    case R_DotsSymbol_x: R_DotsSymbol_static = createGlobalRef(value, 1); break;
    case R_DropSymbol_x: R_DropSymbol_static = createGlobalRef(value, 1); break;
    case R_LastvalueSymbol_x: R_LastvalueSymbol_static = createGlobalRef(value, 1); break;
    case R_LevelsSymbol_x: R_LevelsSymbol_static = createGlobalRef(value, 1); break;
    case R_ModeSymbol_x: R_ModeSymbol_static = createGlobalRef(value, 1); break;
    case R_NameSymbol_x: R_NameSymbol_static = createGlobalRef(value, 1); break;
    case R_NamesSymbol_x: R_NamesSymbol_static = createGlobalRef(value, 1); break;
    case R_NaRmSymbol_x: R_NaRmSymbol_static = createGlobalRef(value, 1); break;
    case R_PackageSymbol_x: R_PackageSymbol_static = createGlobalRef(value, 1); break;
    case R_QuoteSymbol_x: R_QuoteSymbol_static = createGlobalRef(value, 1); break;
    case R_RowNamesSymbol_x: R_RowNamesSymbol_static = createGlobalRef(value, 1); break;
    case R_SeedsSymbol_x: R_SeedsSymbol_static = createGlobalRef(value, 1); break;
    case R_SourceSymbol_x: R_SourceSymbol_static = createGlobalRef(value, 1); break;
    case R_TspSymbol_x: R_TspSymbol_static = createGlobalRef(value, 1); break;
    case R_dot_defined_x: R_dot_defined_static = createGlobalRef(value, 1); break;
    case R_dot_Method_x: R_dot_Method_static = createGlobalRef(value, 1); break;
    case R_dot_target_x: R_dot_target_static = createGlobalRef(value, 1); break;
    case R_SrcrefSymbol_x: R_SrcrefSymbol_static = createGlobalRef(value, 1); break;
    case R_SrcfileSymbol_x: R_SrcfileSymbol_static = createGlobalRef(value, 1); break;
    case R_NaString_x: R_NaString_static = createGlobalRef(value, 1); break;
    case R_BlankString_x: R_BlankString_static = createGlobalRef(value, 1); break;
    case R_BlankScalarString_x: R_BlankString_static = createGlobalRef(value, 1); break;
    case R_BaseSymbol_x: R_BaseSymbol_static = createGlobalRef(value, 1); break;
    case R_NamespaceEnvSymbol_x: R_NamespaceEnvSymbol_static = createGlobalRef(value, 1); break;
    // case R_RestartToken_x: R_RestartToken_static = createGlobalRef(value, 1); break;
    default:
    	printf("Call_initvar_obj: unimplemented index %d\n", index);
    	exit(1);
	}
}
