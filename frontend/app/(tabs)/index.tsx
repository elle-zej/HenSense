import { Image, StyleSheet, Platform } from 'react-native';

import { HelloWave } from '@/components/HelloWave';
import ParallaxScrollView from '@/components/ParallaxScrollView';
import { ThemedText } from '@/components/ThemedText';
import { ThemedView } from '@/components/ThemedView';
import HomePage  from '@/components/homepage';

export default function HomeScreen() {
  return (
    <div>
      <HomePage />
    </div>
  );
}