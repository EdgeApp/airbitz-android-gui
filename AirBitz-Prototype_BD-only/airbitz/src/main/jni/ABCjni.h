#include <jni.h>
/* Header for class com_airbitz_api_AirbitzAPI */

#ifndef _Included_com_airbitz_api_AirbitzAPI
#define _Included_com_airbitz_api_AirbitzAPI
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCInitialize
 * Signature: (Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/BitcoinEventCallback;Lcom/airbitz/api/AirbitzAPI;[CILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABCInitialize
  (JNIEnv *, jobject, jstring, jobject, jobject, jcharArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCTerminate
 * Signature:
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABCTerminate
  (JNIEnv *, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCClearKeyCache
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ClearKeyCache
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSignIn
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SignIn
  (JNIEnv *, jobject, jstring, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCreateAccount
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateAccount
  (JNIEnv *, jobject, jstring, jstring, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSetAccountRecoveryQuestions
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetAccountRecoveryQuestions
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCreateWallet
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateWallet
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jint, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetCurrencies
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCCurrency;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetCurrencies
  (JNIEnv *, jobject, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetPIN
 * Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetPIN
  (JNIEnv *, jobject, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSetPIN
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetPIN
  (JNIEnv *, jobject, jstring, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetCategories
 * Signature: (Ljava/lang/String;[[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetCategories
  (JNIEnv *, jobject, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCAddCategory
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1AddCategory
  (JNIEnv *, jobject, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCRemoveCategory
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1RemoveCategory
  (JNIEnv *, jobject, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCRenameWallet
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1RenameWallet
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSetWalletAttributes
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetWalletAttributes
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCheckRecoveryAnswers
 * Signature: (Ljava/lang/String;Ljava/lang/String;ZLcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CheckRecoveryAnswers
  (JNIEnv *, jobject, jstring, jstring, jboolean, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetWalletInfo
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetWalletInfo
  (JNIEnv *, jobject, jstring, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeWalletInfo
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeWalletInfo
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetWallets
 * Signature: (Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetWallets
  (JNIEnv *, jobject, jstring, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeWalletInfoArray
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;I)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeWalletInfoArray
  (JNIEnv *, jobject, jobjectArray, jint);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSetWalletOrder
 * Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetWalletOrder
  (JNIEnv *, jobject, jstring, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetQuestionChoices
 * Signature: (Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetQuestionChoices
  (JNIEnv *, jobject, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeQuestionChoices
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCQuestionChoices;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeQuestionChoices
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetRecoveryQuestions
 * Signature: (Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetRecoveryQuestions
  (JNIEnv *, jobject, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCChangePassword
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ChangePassword
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCChangePasswordWithRecoveryAnswers
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ChangePasswordWithRecoveryAnswers
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCParseBitcoinURI
 * Signature: (Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCBitcoinURIInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ParseBitcoinURI
  (JNIEnv *, jobject, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeURIInfo
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCBitcoinURIInfo;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeURIInfo
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSatoshiToBitcoin
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SatoshiToBitcoin
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCBitcoinToSatoshi
 * Signature: (D)J
 */
JNIEXPORT jlong JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1BitcoinToSatoshi
  (JNIEnv *, jobject, jdouble);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSatoshiToCurrency
 * Signature: (JDILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SatoshiToCurrency
  (JNIEnv *, jobject, jlong, jdouble, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCurrencyToSatoshi
 * Signature: (DIJLcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CurrencyToSatoshi
  (JNIEnv *, jobject, jdouble, jint, jlong, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCreateReceiveRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateReceiveRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jobject, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCModifyReceiveRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ModifyReceiveRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFinalizeReceiveRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FinalizeReceiveRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCancelReceiveRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CancelReceiveRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGenerateRequestQRCode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GenerateRequestQRCode
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCInitiateSendRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1InitiateSendRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetTransaction
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransaction
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetTransactions
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransactions
  (JNIEnv *, jobject, jstring, jstring, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeTransaction
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTransaction
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeTransactions
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;I)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTransactions
  (JNIEnv *, jobject, jobjectArray, jint);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCSetTransactionDetails
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetTransactionDetails
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetTransactionDetails
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransactionDetails
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetRequestAddress
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetRequestAddress
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCGetPendingRequests
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCRequestInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetPendingRequests
  (JNIEnv *, jobject, jstring, jstring, jstring, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeRequests
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCRequestInfo;I)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeRequests
  (JNIEnv *, jobject, jobjectArray, jint);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCDuplicateTxDetails
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1DuplicateTxDetails
  (JNIEnv *, jobject, jobjectArray, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeTxDetails
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTxDetails
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCCheckPassword
 * Signature: (Ljava/lang/String;D[[Lcom/airbitz/api/AirbitzAPI/ABCPasswordRule;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CheckPassword
  (JNIEnv *, jobject, jstring, jdouble, jobjectArray, jint, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreePasswordRuleArray
 * Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCPasswordRule;I)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreePasswordRuleArray
  (JNIEnv *, jobject, jobjectArray, jint);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCLoadAccountSettings
 * Signature: (Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1LoadAccountSettings
  (JNIEnv *, jobject, jstring, jstring, jobjectArray, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCUpdateAccountSettings
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
 */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1UpdateAccountSettings
  (JNIEnv *, jobject, jstring, jstring, jobject, jobject);

/*
 * Class:     com_airbitz_api_AirbitzAPI
 * Method:    ABCFreeAccountSettings
 * Signature: (Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;)V
 */
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeAccountSettings
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif