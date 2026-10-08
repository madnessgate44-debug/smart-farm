package com.ahmedpasha.smartfarm.balegh
interface BaleghUnderstandingProvider { suspend fun analyze(input: BaleghInput, context: TargetedFarmContext): BaleghAnalysis }
