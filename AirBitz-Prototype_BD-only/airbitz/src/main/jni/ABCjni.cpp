/*
 *
 */
#include <ABCjni.h>
#include <ABC.h>

///*
// * Class:     com_airbitz_api_AirbitzAPI
// * Method:    ABCInitialize
// * Signature: (Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/BitcoinEventCallback;Lcom/airbitz/api/AirbitzAPI;[CILcom/airbitz/api/AirbitzAPI/ABCError;)I
// */
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABCInitialize
  (JNIEnv *env,
  jobject thiz,
  jstring rootDir,
  jobject callback,
  jobject pData,
  jcharArray pSeedData,
  jint seedLength,
  jobject pError)
  {
//        ABC_Initialize(rootDir, callback, pData, pSeedData, seedLength, pError);
    return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCTerminate
* Signature: (;)I
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABCTerminate
  (JNIEnv *env,
  jobject thiz)
  {
    ABC_Terminate();
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCClearKeyCache
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ClearKeyCache
  (JNIEnv *env,
  jobject thiz,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSignIn
* Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SignIn
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCreateAccount
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateAccount
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szPIN,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSetAccountRecoveryQuestions
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetAccountRecoveryQuestions
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szRecoveryQuestions,
  jstring szRecoveryAnswers,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCreateWallet
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IILcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateWallet
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletName,
  jint currencyNum,
  jint attributes,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetCurrencies
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCCurrency;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetCurrencies
  (JNIEnv *env,
  jobject thiz,
  jobjectArray paCurrencyArray,
  jint pCount,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetPIN
* Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetPIN
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobjectArray pszPIN,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSetPIN
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetPIN
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szPIN,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetCategories
* Signature: (Ljava/lang/String;[[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetCategories
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jobjectArray paszCategories,
  jint pCount,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCAddCategory
* Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1AddCategory
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szCategory,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCRemoveCategory
* Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1RemoveCategory
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szCategory,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCRenameWallet
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1RenameWallet
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szUUID,
  jstring szNewWalletName,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSetWalletAttributes
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetWalletAttributes
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szUUID,
  jint attributes,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCheckRecoveryAnswers
* Signature: (Ljava/lang/String;Ljava/lang/String;ZLcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CheckRecoveryAnswers
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szRecoveryAnswers,
  jboolean pbValid,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetWalletInfo
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetWalletInfo
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szUUID,
  jobjectArray ppWalletInfo,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeWalletInfo
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeWalletInfo
  (JNIEnv *env,
  jobject thiz,
  jobject pWalletInfo)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetWallets
* Signature: (Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetWallets
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobjectArray paWalletInfo,
  jint pCount,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeWalletInfoArray
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCWalletInfo;I)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeWalletInfoArray
  (JNIEnv *env,
  jobject thiz,
  jobjectArray aWalletInfo,
  jint nCount)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSetWalletOrder
* Signature: (Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetWalletOrder
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobjectArray aszUUIDArray,
  jint countUUIDs,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetQuestionChoices
* Signature: (Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetQuestionChoices
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeQuestionChoices
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCQuestionChoices;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeQuestionChoices
  (JNIEnv *env,
  jobject thiz,
  jobject pQuestionChoices)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetRecoveryQuestions
* Signature: (Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetRecoveryQuestions
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jobjectArray pszQuestions,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCChangePassword
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ChangePassword
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szNewPassword,
  jstring szNewPIN,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCChangePasswordWithRecoveryAnswers
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ChangePasswordWithRecoveryAnswers
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szRecoveryAnswers,
  jstring szNewPassword,
  jstring szNewPIN,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCParseBitcoinURI
* Signature: (Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCBitcoinURIInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ParseBitcoinURI
  (JNIEnv *env,
  jobject thiz,
  jstring szURI,
  jobjectArray ppInfo,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeURIInfo
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCBitcoinURIInfo;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeURIInfo
  (JNIEnv *env,
  jobject thiz,
  jobject pInfo)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSatoshiToBitcoin
* Signature: (J)D
*/
JNIEXPORT jdouble JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SatoshiToBitcoin
  (JNIEnv *env,
  jobject thiz,
  jlong satoshi)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCBitcoinToSatoshi
* Signature: (D)J
*/
JNIEXPORT jlong JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1BitcoinToSatoshi
  (JNIEnv *env,
  jobject thiz,
  jdouble bitcoin)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSatoshiToCurrency
* Signature: (JDILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SatoshiToCurrency
  (JNIEnv *env,
  jobject thiz,
  jlong satoshi,
  jdouble pCurrency,
  jint currencyNum,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCurrencyToSatoshi
* Signature: (DIJLcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CurrencyToSatoshi
  (JNIEnv *env,
  jobject thiz,
  jdouble currency,
  jint currencyNum,
  jlong pSatoshi,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCreateReceiveRequest
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CreateReceiveRequest
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jobject pDetails,
  jobjectArray pszRequestID,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCModifyReceiveRequest
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1ModifyReceiveRequest
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szRequestID,
  jobject pDetails,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFinalizeReceiveRequest
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FinalizeReceiveRequest
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szRequestID,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCancelReceiveRequest
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CancelReceiveRequest
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szRequestID,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGenerateRequestQRCode
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GenerateRequestQRCode
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szRequestID,
  jobjectArray paData,
  jint pWidth,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCInitiateSendRequest
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/RequestCallback;Lcom/airbitz/api/AirbitzAPI;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1InitiateSendRequest
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szDestAddress,
  jobject pDetails,
  jobject fRequestCallback,
  jobject pData,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetTransaction
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransaction
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szID,
  jobjectArray ppTransaction,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetTransactions
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransactions
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jobjectArray paTransactions,
  jint pCount,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeTransaction
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTransaction
  (JNIEnv *env,
  jobject thiz,
  jobject pTransaction)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeTransactions
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCTxInfo;I)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTransactions
  (JNIEnv *env,
  jobject thiz,
  jobjectArray aTransactions,
  jint count)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCSetTransactionDetails
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1SetTransactionDetails
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szID,
  jobject pDetails,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetTransactionDetails
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetTransactionDetails
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szID,
  jobjectArray ppDetails,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetRequestAddress
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetRequestAddress
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jstring szRequestID,
  jobjectArray pszAddress,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCGetPendingRequests
* Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[[Lcom/airbitz/api/AirbitzAPI/ABCRequestInfo;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1GetPendingRequests
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jstring szWalletUUID,
  jobjectArray paRequests,
  jint pCount,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeRequests
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCRequestInfo;I)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeRequests
  (JNIEnv *env,
  jobject thiz,
  jobjectArray aRequests,
  jint count)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCDuplicateTxDetails
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1DuplicateTxDetails
  (JNIEnv *env,
  jobject thiz,
  jobjectArray ppNewDetails,
  jobject pOldDetails,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeTxDetails
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCTxDetails;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeTxDetails
  (JNIEnv *env,
  jobject thiz,
  jobject pDetails)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCCheckPassword
* Signature: (Ljava/lang/String;D[[Lcom/airbitz/api/AirbitzAPI/ABCPasswordRule;ILcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1CheckPassword
  (JNIEnv *env,
  jobject thiz,
  jstring szPassword,
  jdouble pSecondsToCrack,
  jobjectArray paRules,
  jint pCountRules,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreePasswordRuleArray
* Signature: ([Lcom/airbitz/api/AirbitzAPI/ABCPasswordRule;I)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreePasswordRuleArray
  (JNIEnv *env,
  jobject thiz,
  jobjectArray aRules,
  jint nCount)
  {
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCLoadAccountSettings
* Signature: (Ljava/lang/String;Ljava/lang/String;[Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1LoadAccountSettings
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobjectArray ppSettings,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCUpdateAccountSettings
* Signature: (Ljava/lang/String;Ljava/lang/String;Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;Lcom/airbitz/api/AirbitzAPI/ABCError;)I
*/
JNIEXPORT jint JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1UpdateAccountSettings
  (JNIEnv *env,
  jobject thiz,
  jstring szUserName,
  jstring szPassword,
  jobject pSettings,
  jobject pError)
  {
        return 0;
  }

/*
* Class:     com_airbitz_api_AirbitzAPI
* Method:    ABCFreeAccountSettings
* Signature: (Lcom/airbitz/api/AirbitzAPI/ABCAccountSettings;)V
*/
JNIEXPORT void JNICALL Java_com_airbitz_api_AirbitzAPI_ABC1FreeAccountSettings
  (JNIEnv *env,
  jobject thiz,
  jobject pSettings)
  {
  }
